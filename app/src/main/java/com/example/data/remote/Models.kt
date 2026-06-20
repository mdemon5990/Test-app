package com.example.data.remote

enum class ClaudeModel(val id: String, val displayName: String) {
    OPUS_4_8("opus 4.8", "Opus 4.8"),
    OPUS_4_5("opus 4.5", "Opus 4.5"),
    OPUS_4_6("opus 4.6", "Opus 4.6"),
    HAIKU("Haiku", "Haiku"),
    CLAUDE_3_OPUS("claude-3-opus-20240229", "Claude 3 Opus (Official)"),
    CLAUDE_3_SONNET("claude-3-5-sonnet-20240620", "Claude 3.5 Sonnet (Official)"),
    CLAUDE_3_HAIKU("claude-3-haiku-20240307", "Claude 3 Haiku (Official)")
}
