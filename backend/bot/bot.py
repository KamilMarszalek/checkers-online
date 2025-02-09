import copy
import math
import random

WON_PRIZE = 10000
MINIMAX_DEPTH = 5


class Bot:
    def __init__(self, my_color):
        self.my_color = my_color
        self.board = self._initialize_board()
        self.current_player = "white"

    def _initialize_board(self):
        board = [[None for _ in range(8)] for _ in range(8)]
        for row in range(3):
            for col in range(8):
                if (row + col) % 2 == 1:
                    board[row][col] = "b"
        for row in range(5, 8):
            for col in range(8):
                if (row + col) % 2 == 1:
                    board[row][col] = "w"
        return board

    def make_local_move(self, board, move):
        (fr, fc, tr, tc) = move
        new_board = copy.deepcopy(board)
        piece = new_board[fr][fc]

        new_board[fr][fc] = None
        new_board[tr][tc] = piece

        if abs(tr - fr) == 2 and abs(tc - fc) == 2:
            captured_r = (fr + tr) // 2
            captured_c = (fc + tc) // 2
            new_board[captured_r][captured_c] = None

        if piece == "w" and tr == 0:
            new_board[tr][tc] = "W"
        elif piece == "b" and tr == 7:
            new_board[tr][tc] = "B"

        return new_board

    def get_all_moves(self, board, player_color):
        moves = []
        has_capture = False

        for r in range(8):
            for c in range(8):
                piece = board[r][c]
                if piece is None:
                    continue
                if (piece.lower() == "w" and player_color == "white") or (
                        piece.lower() == "b" and player_color == "black"
                ):
                    piece_moves = self.get_piece_moves(board, r, c)
                    captures = [m for m in piece_moves if abs(m[2] - r) == 2]
                    if captures:
                        has_capture = True
                    moves.extend([(r, c, m[2], m[3]) for m in piece_moves])

        if has_capture:
            captures = [mv for mv in moves if abs(mv[2] - mv[0]) == 2]
            return captures

        return moves

    def get_piece_moves(self, board, row, col):
        piece = board[row][col]
        moves = []
        if piece is None:
            return moves

        directions = []
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
                if cap_piece is not None:
                    if piece.lower() != cap_piece.lower():
                        if board[r_landing][c_landing] is None:
                            moves.append((row, col, r_landing, c_landing))

        return moves

    def evaluate_board(self, board):
        if self.is_terminal(board):
            white_moves = self.get_all_moves(board, "white")
            black_moves = self.get_all_moves(board, "black")
            if len(white_moves) == 0:
                return -WON_PRIZE
            elif len(black_moves) == 0:
                return WON_PRIZE
        score = 0
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

    def is_terminal(self, board):
        white_moves = self.get_all_moves(board, "white") or []
        black_moves = self.get_all_moves(board, "black") or []
        return (len(white_moves) == 0) or (len(black_moves) == 0)

    def minimax(self, board, depth, alpha, beta, maximizing_player):
        if depth == 0 or self.is_terminal(board):
            return self.evaluate_board(board), None

        if maximizing_player:
            best_value = -math.inf
            best_moves = []
            moves = self.get_all_moves(board, "white")
            if not moves:
                return self.evaluate_board(board), None

            for move in moves:
                new_board = self.make_local_move(board, move)
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
            best_value = math.inf
            best_moves = []
            moves = self.get_all_moves(board, "black")
            if not moves:
                return self.evaluate_board(board), None

            for move in moves:
                new_board = self.make_local_move(board, move)
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

    def choose_best_move(self, depth=MINIMAX_DEPTH):
        maximizing = self.current_player == "white"
        _, best_moves = self.minimax(self.board, depth, -math.inf, math.inf, maximizing)
        if best_moves:
            return random.choice(best_moves)
