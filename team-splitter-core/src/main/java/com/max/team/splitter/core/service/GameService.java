package com.max.team.splitter.core.service;

import com.max.team.splitter.core.converter.CoreConverters;
import com.max.team.splitter.core.model.Game;
import com.max.team.splitter.core.model.Player;
import com.max.team.splitter.persistence.entities.GameEntity;
import com.max.team.splitter.persistence.entities.TeamEntryEntity;
import com.max.team.splitter.persistence.repositories.GameRepository;
import com.max.team.splitter.persistence.repositories.TeamEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.max.team.splitter.core.service.Constants.TEAM_COLORS;

@Service
public class GameService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final GameRepository gameRepository;
    private final TeamEntryRepository teamEntryRepository;
    private final PlayerService playerService;

    public GameService(GameRepository gameRepository, TeamEntryRepository teamEntryRepository, PlayerService playerService) {
        this.gameRepository = gameRepository;
        this.teamEntryRepository = teamEntryRepository;
        this.playerService = playerService;
    }

    public List<Game> getGameByPoll(String pollId) {
        List<GameEntity> gameEntities = gameRepository.findByPollId(pollId);
        List<TeamEntryEntity> entities = teamEntryRepository.findByGameIdIn(gameEntities.stream().map(GameEntity::getId).collect(Collectors.toList()));
        Map<Long, List<TeamEntryEntity>> gameToEntries = entities.stream().collect(Collectors.groupingBy(TeamEntryEntity::getGameId));
        Map<Long, Player> playerMap = getPlayersMap(entities);

        List<Game> games = gameEntities.stream().map(CoreConverters::toGame).collect(Collectors.toList());
        for (Game game : games) {
            if (gameToEntries.containsKey(game.getId())) {
                Map<String, List<TeamEntryEntity>> byTeamName = gameToEntries.get(game.getId()).stream().collect(Collectors.groupingBy(TeamEntryEntity::getTeamName));
                Map<String, List<Player>> teams = new LinkedHashMap<>();
                for (Map.Entry<String, List<TeamEntryEntity>> entry : byTeamName.entrySet()) {
                    teams.put(entry.getKey(), entry.getValue().stream().map((item) -> playerMap.get(item.getPlayerId())).collect(Collectors.toList()));
                }
                game.setTeams(teams);
            } else {
                game.setTeams(Collections.emptyMap());
            }
        }
        games.forEach((game -> {
            Map<String, List<Player>> teams = game.getTeams();
            LinkedHashMap<String, List<Player>> sortedByTeamColor = new LinkedHashMap<>();
            for (String teamColor : TEAM_COLORS) {
                if (teams.containsKey(teamColor)) {
                    sortedByTeamColor.put(teamColor, teams.get(teamColor));
                }
            }
            game.setTeams(sortedByTeamColor);
        }));

        return games;
    }

    private Map<Long, Player> getPlayersMap(List<TeamEntryEntity> teamEntryEntities) {
        Set<Long> ids = teamEntryEntities.stream().map(TeamEntryEntity::getPlayerId).collect(Collectors.toSet());
        List<Player> playersByIds = playerService.getPlayersByIds(ids);
        Map<Long, Player> playerMap = playersByIds.stream().collect(Collectors.toMap(Player::getId, Function.identity()));
        return playerMap;
    }

    public void saveGameSplit(Map<String, List<Player>> teams, String pollId) {
        GameEntity entity = new GameEntity();
        entity.setCreationTimestamp(Instant.now());
        entity.setPollId(pollId);

        GameEntity savedGame = gameRepository.save(entity);
        List<TeamEntryEntity> teamEntryEntities = new LinkedList<>();
        for (Map.Entry<String, List<Player>> entry : teams.entrySet()) {
            String teamColor = entry.getKey();
            List<Player> players = entry.getValue();
            for (Player player : players) {
                TeamEntryEntity teamEntry = new TeamEntryEntity();
                teamEntry.setTeamName(teamColor);
                teamEntry.setPlayerId(player.getId());
                teamEntry.setGameId(savedGame.getId());
                teamEntry.setScore(player.getScore());
                teamEntryEntities.add(teamEntry);
            }
        }

        teamEntryRepository.saveAll(teamEntryEntities);
    }
}