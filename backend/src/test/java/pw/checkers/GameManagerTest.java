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
import pw.checkers.game.GameService;
import pw.checkers.message.GameIdMessage;
import pw.checkers.message.Move;
import pw.checkers.message.MoveOutput;
import pw.checkers.message.PossibilitiesInput;
import pw.checkers.message.PossibilitiesOutput;
import pw.checkers.sockets.GameManager;
import pw.checkers.sockets.SessionManager;

@ExtendWith(MockitoExtension.class)
public class GameManagerTest {

    @Mock
    private GameService gameService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private WebSocketSession session;

    private GameManager gameManager;

    @BeforeEach
    public void setUp() {
        gameManager = new GameManager(gameService, sessionManager);
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
        MoveOutput expectedOutput = new MoveOutput();
        expectedOutput.setMove(move);
        when(gameService.makeMove(gameId, move, color)).thenReturn(expectedOutput);

        // When
        MoveOutput result = gameManager.makeMove(gameId, move, color);

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
        PossibilitiesInput input = new PossibilitiesInput(gameId, 2, 3);
        // Stub SessionManager to return an empty Optional.
        when(sessionManager.getAssignedColor(gameId, session)).thenReturn(Optional.empty());

        // When
        PossibilitiesOutput result = gameManager.getPossibleMoves(input, session);

        // Then
        assertNull(result, "When no color is assigned, getPossibleMoves should return null.");
    }

    @Test
    public void testGetPossibleMoves_WithAssignedColor() throws IOException {
        // Given
        String gameId = "game123";
        int row = 2;
        int col = 3;
        PossibilitiesInput input = new PossibilitiesInput(gameId, row, col);
        // Stub SessionManager to return an assigned color.
        when(sessionManager.getAssignedColor(gameId, session)).thenReturn(Optional.of("white"));

        // Create a fake GameState and stub gameService.getGame
        GameState dummyState = new GameState();
        dummyState.setGameId(gameId);
        when(gameService.getGame(gameId)).thenReturn(dummyState);

        // Create fake PossibleMoves and stub gameService.getPossibleMoves
        PossibilitiesOutput expectedMoves = new PossibilitiesOutput();
        when(gameService.getPossibleMoves(dummyState, row, col)).thenReturn(expectedMoves);

        // When
        PossibilitiesOutput result = gameManager.getPossibleMoves(input, session);

        // Then
        assertEquals(expectedMoves, result);
    }
}
