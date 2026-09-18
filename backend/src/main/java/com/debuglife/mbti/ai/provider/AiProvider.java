package com.debuglife.mbti.ai.provider;

import java.util.List;
import java.util.function.Consumer;

public interface AiProvider {

    String provider();

    String model();

    String complete(List<AiMessage> messages);

    void stream(List<AiMessage> messages, Consumer<String> onDelta);
}