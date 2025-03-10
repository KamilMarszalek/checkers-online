package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import pw.checkers.data.GameState;
import pw.checkers.data.enums.Color;
import pw.checkers.game.GameEndManager;
import pw.checkers.game.GameService;
import pw.checkers.message.*;
import pw.checkers.sockets.services.GameManager;
import pw.checkers.sockets.services.SessionManager;

@ExtendWith(MockitoExtension.class)
public class GameManagerTest {

    @Mock
    private GameService gameService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private GameEndManager gameEndManager;

    @Mock
    private WebSocketSession session;

    private GameManager gameManager;

    @BeforeEach
    public void setUp() {
        gameManager = new GameManager(gameService, sessionManager, gameEndManager);
    }

    @Test
    public void testCleanGameHistory() {
        // Given
        String gameId = "game123";
        GameIdMessage gameIdMessage = new GameIdMessage(gameId);

        // When
        gameManager.cleanGameHistory(gameIdMessage);

        // Then: verify that SessionManager and GameService are invoked
        verify(sessionManager, times(1)).removeGameFromMaps(gameId);
        verify(gameService, times(1)).deleteGame(gameId);
    }

    @Test
    public void testCreateGame() {
        // Given a fake game state with a specific gameId
        String gameId = "game123";
        GameState dummyGame = new GameState();
        dummyGame.setGameId(gameId);
        when(gameService.createGame()).thenReturn(dummyGame);

        // When
        String result = gameManager.createGame();

        // Then
        assertEquals(gameId, result);
    }

    @Test
    public void testMakeMove() {
        // Given
        String gameId = "game123";
        Move move = new Move(1, 2, 3, 4);
        String color = "white";
        MoveOutputMessage expectedOutput = new MoveOutputMessage();
        expectedOutput.setMove(move);
        when(gameService.makeMove(gameId, move, color)).thenReturn(expectedOutput);

        // When
        MoveOutputMessage result = gameManager.makeMove(gameId, move, color);

        // Then
        assertEquals(expectedOutput, result);
    }

    @Test
    public void testGetGame() {
        // Given
        String gameId = "game123";
        GameState expectedState = new GameState();
        expectedState.setGameId(gameId);
        when(gameService.getGame(gameId)).thenReturn(expectedState);

        // When
        GameState result = gameManager.getGame(gameId);

        // Then
        assertEquals(expectedState, result);
    }

    @Test
    public void testGetPossibleMoves_NoAssignedColorReturnsNull() throws IOException {
        // Given
        String gameId = "game123";
        // Create a PossibilitiesInput with fake values.
        PossibilitiesInputMessage input = new PossibilitiesInputMessage(gameId, 2, 3);
        // Stub SessionManager to return an empty Optional.
        when(sessionManager.getAssignedColor(gameId, session)).thenReturn(Optional.empty());

        // When
        PossibilitiesOutputMessage result = gameManager.getPossibleMoves(input, session);

        // Then
        assertNull(result, "When no color is assigned, getPossibleMoves should return null.");
    }

    @Test
    public void testGetPossibleMoves_WithAssignedColor() throws IOException {
        // Given
        String gameId = "game123";
        int row = 2;
        int col = 3;
        PossibilitiesInputMessage input = new PossibilitiesInputMessage(gameId, row, col);
        // Stub SessionManager to return an assigned color.
        when(sessionManager.getAssignedColor(gameId, session)).thenReturn(Optional.of("white"));

        // Create a fake GameState and stub gameService.getGame
        GameState dummyState = new GameState();
        dummyState.setGameId(gameId);
        when(gameService.getGame(gameId)).thenReturn(dummyState);

        // Create fake PossibleMoves and stub gameService.getPossibleMoves
        PossibilitiesOutputMessage expectedMoves = new PossibilitiesOutputMessage();
        when(gameService.getPossibleMoves(dummyState, row, col)).thenReturn(expectedMoves);

        // When
        PossibilitiesOutputMessage result = gameManager.getPossibleMoves(input, session);

        // Then
        assertEquals(expectedMoves, result);
    }

    @Test
    public void testSetGameEnd() throws IOException {
        String gameId = "game123";
        GameState dummyGame = new GameState();
        dummyGame.setGameId(gameId);
        when(gameService.createGame()).thenReturn(dummyGame);

        when(gameService.getGame(gameId)).thenReturn(dummyGame);

        gameManager.createGame();
        ResignMessage message = new ResignMessage();
        message.setGameId(gameId);
        message.setType("resign");
        GameState updatedState = gameManager.setGameEnd(message, "white");
        assertEquals(gameId, updatedState.getGameId());
        assertTrue(updatedState.isFinished());
        assertEquals(Color.WHITE, updatedState.getWinner());
    }
}
