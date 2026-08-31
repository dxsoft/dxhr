package com.dxsoft.rsgzgl.security.ukey;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class UkeyChallengeStore {

    private final long ttlSeconds;
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    UkeyChallengeStore(@Value("${rsgzgl.ukey.challenge-ttl-seconds:120}") long ttlSeconds) {
        this.ttlSeconds = Math.max(30, ttlSeconds);
    }

    ChallengeIssue issue() {
        purgeExpired();
        String challengeId = UUID.randomUUID().toString().replace("-", "");
        // Match SoftKey sample: positive int random used as message string
        String rnd = String.valueOf((int) (Math.random() * 65535) + 1);
        challenges.put(challengeId, new Challenge(rnd, Instant.now().plusSeconds(ttlSeconds)));
        return new ChallengeIssue(challengeId, rnd);
    }

    String consume(String challengeId) {
        if (challengeId == null || challengeId.isBlank()) {
            return null;
        }
        Challenge challenge = challenges.remove(challengeId.trim());
        if (challenge == null) {
            return null;
        }
        if (Instant.now().isAfter(challenge.expireAt())) {
            return null;
        }
        return challenge.rnd();
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        challenges.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expireAt()));
    }

    record ChallengeIssue(String challengeId, String rnd) {
    }

    private record Challenge(String rnd, Instant expireAt) {
    }
}
