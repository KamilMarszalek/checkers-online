from bot import Bot


def test_bot_create():
    bot = Bot("white")
    assert bot.my_color == "white"
    assert bot.current_player == "white"
    assert bot.board == [
        [None, "b", None, "b", None, "b", None, "b"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, "b", None, "b", None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        ["w", None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]
    assert bot._initialize_board() == [
        [None, "b", None, "b", None, "b", None, "b"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, "b", None, "b", None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        ["w", None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]


def test_bot_make_local_move():
    bot = Bot("white")
    board = bot.board
    move = [5, 0, 4, 1]
    new_board = bot.make_local_move(board, move)
    assert new_board == [
        [None, "b", None, "b", None, "b", None, "b"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, "b", None, "b", None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, "w", None, None, None, None, None, None],
        [None, None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]


def test_bot_should_promote():
    bot = Bot("white")
    assert bot.should_promote("w", 0) is True
    assert bot.should_promote("b", 7) is True
    assert bot.should_promote("w", 7) is False
    assert bot.should_promote("b", 0) is False


def test_bot_is_capture():
    bot = Bot("white")
    assert bot.is_capture([5, 0, 3, 2]) is True
    assert bot.is_capture([5, 0, 4, 1]) is False
    assert bot.is_capture([5, 0, 5, 1]) is False
    assert bot.is_capture([5, 0, 5, 2]) is False
    assert bot.is_capture([5, 0, 5, 3]) is False
    assert bot.is_capture([5, 0, 5, 4]) is False
    assert bot.is_capture([5, 0, 5, 5]) is False
    assert bot.is_capture([5, 0, 5, 6]) is False


def test_bot_promote():
    bot = Bot("white")
    board = bot.board
    move = [5, 2, 0, 7]
    bot.promote(board, move)
    assert board == [
        [None, "b", None, "b", None, "b", None, "W"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, "b", None, "b", None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        ["w", None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]


def test_bot_make_capture():
    bot = Bot("white")
    board = bot.board
    board = [
        [None, "b", None, "b", None, "b", None, "b"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, None, None, "b", None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, "b", None, None, None, None, None, None],
        ["w", None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]
    move = [5, 0, 3, 2]
    bot.make_capture(board, move)
    assert board == [
        [None, "b", None, "b", None, "b", None, "b"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, None, None, "b", None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        ["w", None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]


def test_bot_does_piece_match_player():
    bot = Bot("white")
    assert bot.does_piece_match_player("w", "white") is True
    assert bot.does_piece_match_player("w", "black") is False
    assert bot.does_piece_match_player("b", "white") is False
    assert bot.does_piece_match_player("b", "black") is True
    assert bot.does_piece_match_player("W", "white") is True
    assert bot.does_piece_match_player("W", "black") is False
    assert bot.does_piece_match_player("B", "white") is False
    assert bot.does_piece_match_player("B", "black") is True


def test_bot_has_capture():
    bot = Bot("white")
    assert bot.has_capture([[5, 0, 3, 2], [5, 0, 4, 1]]) is True
    assert bot.has_capture([[5, 0, 3, 1], [5, 0, 4, 1]]) is False
    assert bot.has_capture([[5, 0, 4, 1]]) is False
    assert bot.has_capture([[5, 0, 5, 1]]) is False
    assert bot.has_capture([[5, 0, 5, 2]]) is False


def test_bot_filter_capture_moves():
    bot = Bot("white")
    moves = [[5, 0, 3, 2], [5, 0, 4, 1], [5, 0, 5, 2], [5, 0, 5, 3]]
    assert bot.filter_capture_moves(moves) == [[5, 0, 3, 2]]


def test_bot_get_piece_moves():
    bot = Bot("white")
    board = bot.board
    moves = bot.get_piece_moves(board, 5, 0)
    assert moves == [[5, 0, 4, 1]]
    moves = bot.get_piece_moves(board, 5, 2)
    assert moves == [[5, 2, 4, 1], [5, 2, 4, 3]]
    moves = bot.get_piece_moves(board, 5, 4)
    assert moves == [[5, 4, 4, 3], [5, 4, 4, 5]]
    moves = bot.get_piece_moves(board, 5, 6)
    assert moves == [[5, 6, 4, 5], [5, 6, 4, 7]]
    moves = bot.get_piece_moves(board, 2, 1)
    assert moves == [[2, 1, 3, 0], [2, 1, 3, 2]]
    moves = bot.get_piece_moves(board, 2, 3)
    assert moves == [[2, 3, 3, 2], [2, 3, 3, 4]]
    moves = bot.get_piece_moves(board, 2, 5)
    assert moves == [[2, 5, 3, 4], [2, 5, 3, 6]]
    moves = bot.get_piece_moves(board, 2, 7)
    assert moves == [[2, 7, 3, 6]]
    moves = bot.get_piece_moves(board, 0, 1)
    assert moves == []


def test_bot_is_capture_valid():
    bot = Bot("white")
    assert bot.is_capture_valid("w", "b") is True
    assert bot.is_capture_valid("w", "w") is False
    assert bot.is_capture_valid("b", "w") is True
    assert bot.is_capture_valid("b", "b") is False
    assert bot.is_capture_valid("W", "b") is True
    assert bot.is_capture_valid("W", "w") is False
    assert bot.is_capture_valid("B", "w") is True
    assert bot.is_capture_valid("B", "b") is False
    assert bot.is_capture_valid("w", None) is False
    assert bot.is_capture_valid("b", None) is False
    assert bot.is_capture_valid("W", None) is False
    assert bot.is_capture_valid("B", None) is False
    assert bot.is_capture_valid(None, "w") is False
    assert bot.is_capture_valid(None, "b") is False
    assert bot.is_capture_valid(None, "W") is False
    assert bot.is_capture_valid(None, "B") is False
    assert bot.is_capture_valid(None, None) is False


def test_bot_is_in_bounds():
    bot = Bot("white")
    assert bot.is_in_bounds(0, 0) is True
    assert bot.is_in_bounds(0, 7) is True
    assert bot.is_in_bounds(7, 0) is True
    assert bot.is_in_bounds(7, 7) is True
    assert bot.is_in_bounds(-1, 0) is False
    assert bot.is_in_bounds(0, -1) is False
    assert bot.is_in_bounds(8, 0) is False
    assert bot.is_in_bounds(0, 8) is False
    assert bot.is_in_bounds(-1, -1) is False
    assert bot.is_in_bounds(8, 8) is False
    assert bot.is_in_bounds(8, -1) is False
    assert bot.is_in_bounds(-1, 8) is False


def test_bot_get_possible_directions():
    bot = Bot("white")
    assert bot.get_possible_directions("w") == [(-1, -1), (-1, 1)]
    assert bot.get_possible_directions("b") == [(1, -1), (1, 1)]
    assert bot.get_possible_directions("W") == [(-1, -1), (-1, 1), (1, -1), (1, 1)]
    assert bot.get_possible_directions("B") == [(-1, -1), (-1, 1), (1, -1), (1, 1)]
    assert bot.get_possible_directions("x") == []
    assert bot.get_possible_directions("X") == []
    assert bot.get_possible_directions(None) == []


def test_bot_evaluate_board():
    bot = Bot("white")
    board = bot.board
    assert bot.evaluate_board(board, "white") == 0
    board = [
        [None, "b", None, "b", None, "b", None, "b"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, "b", None, "b", None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        ["w", None, None, None, None, None, None, None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]
    assert bot.evaluate_board(board, "black") == -3
    board = [
        [None, "b", None, "b", None, "b", None, "b"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, None, None, None, None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        ["w", None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]
    assert bot.evaluate_board(board, "white") == 2
    board = [
        [None, "b", None, "b", None, "b", None, "b"],
        ["b", None, "b", None, "b", None, "b", None],
        [None, "b", None, "b", None, "b", None, "b"],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        ["W", None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]
    assert bot.evaluate_board(board, "white") == 9
    board = [
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        ["w", None, "w", None, "w", None, "w", None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]
    assert bot.evaluate_board(board, "black") == 10000


def test_is_terminal():
    bot = Bot("white")
    board = bot.board
    assert bot.is_terminal(board, "white") is False
    board = [
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        [None, None, None, None, None, None, None, None],
        [None, "w", None, "w", None, "w", None, "w"],
        ["w", None, "w", None, "w", None, "w", None],
    ]
    assert bot.is_terminal(board, "black") is True
    assert bot.is_terminal(board, "white") is False
