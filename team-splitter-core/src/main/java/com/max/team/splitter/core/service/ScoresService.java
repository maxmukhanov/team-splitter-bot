package com.max.team.splitter.core.service;

import com.max.team.splitter.persistence.entities.PlayerScoreEntity;
import com.max.team.splitter.persistence.repositories.PlayerScoreRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoresService {
    private final PlayerScoreRepository playerScoreRepository;

    public ScoresService(PlayerScoreRepository playerScoreRepository) {
        this.playerScoreRepository = playerScoreRepository;
    }

    public Map<Long, Integer> getScores() {
        List<PlayerScoreEntity> scoreEntities = playerScoreRepository.findAll();

        return scoreEntities.stream()
                .collect(Collectors.toMap(PlayerScoreEntity::getPlayerId, PlayerScoreEntity::getScore));
    }

    public void saveScore(Long playerId, Integer score) {
        PlayerScoreEntity entity = new PlayerScoreEntity();
        entity.setPlayerId(playerId);
        entity.setScore(score);
        playerScoreRepository.save(entity);
    }
}
