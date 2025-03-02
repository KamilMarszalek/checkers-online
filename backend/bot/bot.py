import copy
import math
import random
from typing import List, Optional, Tuple
from constants import MINIMAX_DEPTH, WON_PRIZE, BOARD_SIZE


class Bot:
    def __init__(self, my_color: str) -> None:
        self.my_color: str = my_color
        self.board: List[List[Optional[str]]] = self._initialize_board()
        self.current_player: str = "white"

    def _initialize_board(self) -> List[List[Optional[str]]]:
        board = [[None for _ in range(8)] for _ in range(BOARD_SIZE)]
        for row in range(3):
            for col in range(BOARD_SIZE):
                if (row + col) % 2 == 1:
                    board[row][col] = "b"
        for row in range(5, BOARD_SIZE):
            for col in range(BOARD_SIZE):
                if (row + col) % 2 == 1:
                    board[row][col] = "w"
        return board

    def make_local_move(
        self, board: List[List[Optional[str]]], move: List[int]
    ) -> List[List[Optional[str]]]:
        (fr, fc, tr, tc) = move
        new_board = copy.deepcopy(board)
        piece = new_board[fr][fc]

        new_board[fr][fc] = None
        new_board[tr][tc] = piece

        if self.is_capture(move):
            self.make_capture(new_board, move)

        if self.should_promote(piece, tr):
            self.promote(new_board, move)

        return new_board

    def should_promote(self, piece, tr):
        return (piece == "w" and tr == 0) or (piece == "b" and tr == 7)

    def is_capture(self, move):
        return abs(move[2] - move[0]) == 2 and abs(move[3] - move[1]) == 2

    def promote(self, board, move):
        (_, _, tr, tc) = move
        board[tr][tc] = board[tr][tc].upper()

    def make_capture(self, board, move):
        (fr, fc, tr, tc) = move
        captured_r = (fr + tr) // 2
        captured_c = (fc + tc) // 2
        board[captured_r][captured_c] = None

    def get_all_moves(
        self, board: List[List[Optional[str]]], player_color: str
    ) -> List[Tuple[int, int, int, int]]:
        moves: List[Tuple[int, int, int, int]] = []
        for r in range(BOARD_SIZE):
            for c in range(BOARD_SIZE):
                piece = board[r][c]
                if piece is None:
                    continue
                if self.does_piece_match_player(piece, player_color):
                    piece_moves = self.get_piece_moves(board, r, c)
                    moves.extend([(r, c, m[2], m[3]) for m in piece_moves])

        if self.has_capture(moves):
            moves = self.filter_capture_moves(moves)
        if piece_during_capture:
            moves = self.filter_only_piece_during_capture(moves, piece_during_capture)
        return moves

    def filter_only_piece_during_capture(self, moves, piece_during_capture):
        return [mv for mv in moves if (mv[0], mv[1]) == piece_during_capture]

    def has_capture(self, moves):
        return any(self.is_capture(m) for m in moves)

    def filter_capture_moves(self, moves):
        return [mv for mv in moves if self.is_capture(mv)]

    def does_piece_match_player(self, piece, player_color):
        return (piece.lower() == "w" and player_color == "white") or (
            piece.lower() == "b" and player_color == "black"
        )

    def get_piece_moves(
        self, board: List[List[Optional[str]]], row: int, col: int
    ) -> List[Tuple[int, int, int, int]]:
        piece = board[row][col]
        moves: List[Tuple[int, int, int, int]] = []
        if piece is None:
            return moves
        directions: List[Tuple[int, int]] = self.get_possible_directions(piece)
        for dr, dc in directions:
            r_new = row + dr
            c_new = col + dc
            if self.is_in_bounds(r_new, c_new):
                if board[r_new][c_new] is None:
                    moves.append([row, col, r_new, c_new])
            r_cap = row + dr
            c_cap = col + dc
            r_landing = row + 2 * dr
            c_landing = col + 2 * dc
            if self.is_in_bounds(r_landing, c_landing) and self.is_in_bounds(
                r_cap, c_cap
            ):
                cap_piece = board[r_cap][c_cap]
                if self.is_capture_valid(piece, cap_piece):
                    if board[r_landing][c_landing] is None:
                        moves.append([row, col, r_landing, c_landing])

        return moves

    def is_capture_valid(self, piece, cap_piece):
        if piece is None or cap_piece is None:
            return False
        return piece.lower() != cap_piece.lower()

    def is_in_bounds(self, r: int, c: int) -> bool:
        return 0 <= r < BOARD_SIZE and 0 <= c < BOARD_SIZE

    def get_possible_directions(self, piece: str) -> List[Tuple[int, int]]:
        if not piece:
            return []
        if piece.lower() == "w":
            if piece.islower():
                return [(-1, -1), (-1, 1)]
            else:
                return [(-1, -1), (-1, 1), (1, -1), (1, 1)]
        elif piece.lower() == "b":
            if piece.islower():
                return [(1, -1), (1, 1)]
            else:
                return [(-1, -1), (-1, 1), (1, -1), (1, 1)]
        else:
            return []

    def evaluate_board(self, board: List[List[Optional[str]]], current_player) -> int:
        if self.is_terminal(board, current_player):
            return self.evaluate_terminal_state(board)
        return self.evaluate_non_terminal_state(board)

    def evaluate_terminal_state(self, board: List[List[Optional[str]]]) -> int:
        white_moves = self.get_all_moves(board, "white")
        black_moves = self.get_all_moves(board, "black")
        if len(white_moves) == 0:
            return -WON_PRIZE
        elif len(black_moves) == 0:
            return WON_PRIZE

    def evaluate_non_terminal_state(self, board: List[List[Optional[str]]]) -> int:
        score: int = 0
        for r in range(BOARD_SIZE):
            for c in range(BOARD_SIZE):
                piece = board[r][c]
                if piece is not None:
                    if piece.lower() == "w":
                        val = 1 if piece.islower() else 10
                        score += val
                    else:
                        val = 1 if piece.islower() else 10
                        score -= val
        return score

    def is_terminal(self, board: List[List[Optional[str]]], current_player):
        moves = self.get_all_moves(board, current_player)
        return len(moves) == 0

    def minimax(
        self,
        board: List[List[Optional[str]]],
        depth: int,
        alpha: int,
        beta: int,
        maximizing_player: bool,
    ) -> Tuple[int, Optional[List[Tuple[int, int, int, int]]]]:
        current_player = "white" if maximizing_player else "black"
        if depth == 0 or self.is_terminal(board, current_player):
            return self.evaluate_board(board, current_player), None

        if maximizing_player:
            best_value: int = -math.inf
            best_moves: List[Tuple[int, int, int, int]] = []
            moves: List[Tuple[int, int, int, int]] = self.get_all_moves(board, "white")
            if not moves:
                return self.evaluate_board(board, current_player), None

            for move in moves:
                new_board = self.make_local_move(board, list(move))
                val, _ = self.minimax(new_board, depth - 1, alpha, beta, False)
                if val > best_value:
                    best_value = val
                    best_moves = [move]
                elif val == best_value:
                    best_moves.append(move)
                alpha = max(alpha, best_value)
                if beta <= alpha:
                    break
            return best_value, best_moves
        else:
            best_value: int = math.inf
            best_moves: List[Tuple[int, int, int, int]] = []
            moves: List[Tuple[int, int, int, int]] = self.get_all_moves(board, "black")
            if not moves:
                return self.evaluate_board(board, current_player), None

            for move in moves:
                new_board = self.make_local_move(board, list(move))
                val, _ = self.minimax(new_board, depth - 1, alpha, beta, True)
                if val < best_value:
                    best_value = val
                    best_moves = [move]
                elif val == best_value:
                    best_moves.append(move)
                beta = min(beta, best_value)
                if beta <= alpha:
                    break
            return best_value, best_moves

    def choose_best_move(
        self, depth: int = MINIMAX_DEPTH
    ) -> Optional[Tuple[int, int, int, int]]:
        maximizing: bool = self.current_player == "white"
        _, best_moves = self.minimax(self.board, depth, -math.inf, math.inf, maximizing)
        if best_moves:
            return random.choice(best_moves)
        return None
