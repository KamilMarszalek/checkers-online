import copy
import math
import random
from typing import List, Optional, Tuple

WON_PRIZE: int = 10000
MINIMAX_DEPTH: int = 5


class Bot:
    def __init__(self, my_color: str) -> None:
        self.my_color: str = my_color
        self.board: List[List[Optional[str]]] = self._initialize_board()
        self.current_player: str = "white"

    def _initialize_board(self) -> List[List[Optional[str]]]:
        board: List[List[Optional[str]]] = [[None for _ in range(8)] for _ in range(8)]
        for row in range(3):
            for col in range(8):
                if (row + col) % 2 == 1:
                    board[row][col] = "b"
        for row in range(5, 8):
            for col in range(8):
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

        # capture: if the move is a jump, remove the captured piece
        if abs(tr - fr) == 2 and abs(tc - fc) == 2:
            captured_r = (fr + tr) // 2
            captured_c = (fc + tc) // 2
            new_board[captured_r][captured_c] = None

        # promotion: upgrade to king if reached the last row
        if piece == "w" and tr == 0:
            new_board[tr][tc] = "W"
        elif piece == "b" and tr == 7:
            new_board[tr][tc] = "B"

        return new_board

    def get_all_moves(
        self, board: List[List[Optional[str]]], player_color: str
    ) -> List[Tuple[int, int, int, int]]:
        moves: List[Tuple[int, int, int, int]] = []
        has_capture: bool = False

        for r in range(8):
            for c in range(8):
                piece = board[r][c]
                if piece is None:
                    continue
                if (piece.lower() == "w" and player_color == "white") or (
                    piece.lower() == "b" and player_color == "black"
                ):
                    piece_moves = self.get_piece_moves(board, r, c)
                    # Check for capture moves
                    captures = [m for m in piece_moves if abs(m[2] - r) == 2]
                    if captures:
                        has_capture = True
                    moves.extend([(r, c, m[2], m[3]) for m in piece_moves])

        if has_capture:
            moves = [mv for mv in moves if abs(mv[2] - mv[0]) == 2]
        return moves

    def get_piece_moves(
        self, board: List[List[Optional[str]]], row: int, col: int
    ) -> List[Tuple[int, int, int, int]]:
        piece = board[row][col]
        moves: List[Tuple[int, int, int, int]] = []
        if piece is None:
            return moves

        directions: List[Tuple[int, int]] = []
        if piece.lower() == "w":
            if piece.islower():
                directions = [(-1, -1), (-1, 1)]
            else:
                directions = [(-1, -1), (-1, 1), (1, -1), (1, 1)]
        else:
            if piece.islower():
                directions = [(1, -1), (1, 1)]
            else:
                directions = [(-1, -1), (-1, 1), (1, -1), (1, 1)]

        for dr, dc in directions:
            r_new = row + dr
            c_new = col + dc
            if 0 <= r_new < 8 and 0 <= c_new < 8:
                if board[r_new][c_new] is None:
                    moves.append((row, col, r_new, c_new))

            # Check for capture move
            r_cap = row + dr
            c_cap = col + dc
            r_landing = row + 2 * dr
            c_landing = col + 2 * dc
            if (
                0 <= r_cap < 8
                and 0 <= c_cap < 8
                and 0 <= r_landing < 8
                and 0 <= c_landing < 8
            ):
                cap_piece = board[r_cap][c_cap]
                if cap_piece is not None and piece.lower() != cap_piece.lower():
                    if board[r_landing][c_landing] is None:
                        moves.append((row, col, r_landing, c_landing))

        return moves

    def evaluate_board(self, board: List[List[Optional[str]]]) -> int:
        if self.is_terminal(board):
            white_moves = self.get_all_moves(board, "white")
            black_moves = self.get_all_moves(board, "black")
            if len(white_moves) == 0:
                return -WON_PRIZE
            elif len(black_moves) == 0:
                return WON_PRIZE

        score: int = 0
        for r in range(8):
            for c in range(8):
                piece = board[r][c]
                if piece is not None:
                    if piece.lower() == "w":
                        val = 1 if piece.islower() else 10
                        score += val
                    else:
                        val = 1 if piece.islower() else 10
                        score -= val
        return score

    def is_terminal(self, board: List[List[Optional[str]]]) -> bool:
        white_moves = self.get_all_moves(board, "white")
        black_moves = self.get_all_moves(board, "black")
        return (len(white_moves) == 0) or (len(black_moves) == 0)

    def minimax(
        self,
        board: List[List[Optional[str]]],
        depth: int,
        alpha: int,
        beta: int,
        maximizing_player: bool,
    ) -> Tuple[int, Optional[List[Tuple[int, int, int, int]]]]:
        if depth == 0 or self.is_terminal(board):
            return self.evaluate_board(board), None

        if maximizing_player:
            best_value: int = -math.inf
            best_moves: List[Tuple[int, int, int, int]] = []
            moves: List[Tuple[int, int, int, int]] = self.get_all_moves(board, "white")
            if not moves:
                return self.evaluate_board(board), None

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
                return self.evaluate_board(board), None

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
