package com.marcusprado02.sharedkernel.domain.events.upcast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;


public abstract class JsonPatchUpcaster extends BaseJsonUpcaster {
    protected abstract JsonPatch patch();

    @Override
    public UpcastResult apply(EventEnvelope in, UpcastContext ctx) {
        try {
            var node = readObjectNode(in);
            JsonNode patched = patch().apply(node);
            if (patched.equals(node)) return new UpcastResult.Skipped(in, "no_diff");
            EventEnvelope out = withJson(in, (ObjectNode) patched, toVersion());
            return new UpcastResult.Changed(out, "json_patch");
        } catch (Exception e){
            return new UpcastResult.Failed(in, "json_patch_error", e);
        }
    }
}