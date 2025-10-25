#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
OUT_DIR_DEFAULT="build/generated-sources"
PLUGIN_VERSION="${PLUGIN_VERSION:-1.63.0}"

# Descobre todos os módulos que tenham src/main/proto
mapfile -t PROTO_ROOTS < <(find "$REPO_DIR" -type d -path "*/src/main/proto" | sort)

if [ ${#PROTO_ROOTS[@]} -eq 0 ]; then
  echo "ERRO: nenhum diretório 'src/main/proto' encontrado abaixo de $REPO_DIR"
  exit 1
fi

# Plugin gRPC Java
BIN_DIR="$REPO_DIR/.proto-bin"
mkdir -p "$BIN_DIR"
if [ ! -x "$BIN_DIR/protoc-gen-grpc-java" ]; then
  OS="$(uname -s)"; ARCH="$(uname -m)"
  case "$OS-$ARCH" in
    Linux-x86_64) PLUGIN_FILE="protoc-gen-grpc-java-${PLUGIN_VERSION}-linux-x86_64.exe" ;;
    Darwin-x86_64) PLUGIN_FILE="protoc-gen-grpc-java-${PLUGIN_VERSION}-osx-x86_64.exe" ;;
    Darwin-arm64) PLUGIN_FILE="protoc-gen-grpc-java-${PLUGIN_VERSION}-osx-aarch_64.exe" ;;
    *) echo "ERRO: plataforma não mapeada ($OS-$ARCH)"; exit 1 ;;
  esac
  curl -fsSL -o "$BIN_DIR/protoc-gen-grpc-java.exe" \
    "https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/${PLUGIN_VERSION}/${PLUGIN_FILE}"
  chmod +x "$BIN_DIR/protoc-gen-grpc-java.exe"
  ln -sf "$BIN_DIR/protoc-gen-grpc-java.exe" "$BIN_DIR/protoc-gen-grpc-java"
fi

# googleapis (para google/rpc/*.proto)
GA_DIR="$REPO_DIR/third_party/googleapis"
[ -d "$GA_DIR" ] || git clone --depth 1 https://github.com/googleapis/googleapis.git "$GA_DIR"

# well-known types
WKT_INCLUDE="/usr/include"
[ -d "$WKT_INCLUDE/google/protobuf" ] || WKT_INCLUDE=""

for PROTO_ROOT in "${PROTO_ROOTS[@]}"; do
  MODULE_DIR="$(dirname "$(dirname "$PROTO_ROOT")")" # .../module/src/main
  MODULE_DIR="$(dirname "$MODULE_DIR")"              # .../module
  OUT_DIR="$MODULE_DIR/$OUT_DIR_DEFAULT"
  mkdir -p "$OUT_DIR"

  echo "=> Gerando a partir de: $PROTO_ROOT"
  mapfile -t PROTOS < <(find "$PROTO_ROOT" -name "*.proto")
  if [ ${#PROTOS[@]} -eq 0 ]; then
    echo "   (nenhum .proto aqui)"
    continue
  fi

  # Mensagens
  protoc \
    --proto_path="$PROTO_ROOT" \
    ${WKT_INCLUDE:+-I "$WKT_INCLUDE"} \
    -I "$GA_DIR" \
    --java_out="$OUT_DIR" \
    "${PROTOS[@]}"

  # Stubs gRPC
  protoc \
    --proto_path="$PROTO_ROOT" \
    ${WKT_INCLUDE:+-I "$WKT_INCLUDE"} \
    -I "$GA_DIR" \
    --plugin=protoc-gen-grpc-java="$BIN_DIR/protoc-gen-grpc-java" \
    --grpc-java_out="$OUT_DIR" \
    "${PROTOS[@]}"

  echo "   -> Gerados em: $OUT_DIR"
done

echo "OK. Marque cada '$OUT_DIR_DEFAULT' como Source Root na IDE."
