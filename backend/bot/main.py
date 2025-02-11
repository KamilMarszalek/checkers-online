import websocket
import json
import threading
import time
import sys
import argparse

from bot import Bot

URL = f"ws://localhost:8080/ws"

bot = None
my_color = None
game_id = None
captured_white = 0
captured_black = 0


def on_message(ws_app, message):
    global bot, my_color, game_id, captured_white, captured_black

    dict_message = json.loads(message)
    msg_type = dict_message.get("type")

    print("Received:", dict_message)

    if msg_type == "Game created":
        game_id = dict_message["content"]["gameId"]
        assigned_color = dict_message["content"]["color"]
        my_color = assigned_color
        print(f"Assigned color: {my_color}, game_id={game_id}")

        bot = Bot(my_color)

        if my_color == "white":
            bot.current_player = "white"
            do_bot_move(ws_app)
        else:
            bot.current_player = "white"

    elif msg_type == "move":
        move_content = dict_message["content"]

        fr = move_content["move"]["fromRow"]
        fc = move_content["move"]["fromCol"]
        tr = move_content["move"]["toRow"]
        tc = move_content["move"]["toCol"]

        bot.board = bot.make_local_move(bot.board, (fr, fc, tr, tc))

        bot.current_player = move_content["turn"]

        if (my_color == "white" and bot.current_player == "white") or (
                my_color == "black" and bot.current_player == "black"
        ):
            do_bot_move(ws_app)

    elif msg_type == "gameEnd":
        result = dict_message["content"]["result"]
        print(f"End of the game! Result: {result}")
        print("Captured black:", captured_black)
        print("Captured white:", captured_white)
        ws_app.close()

    elif msg_type == "waiting":
        print("Waiting for an opponent...")

    else:
        print("Unknown message type", msg_type)


def do_bot_move(ws_app):
    global bot, my_color, game_id, captured_white, captured_black

    best_move = bot.choose_best_move(depth=6)
    if best_move is None:
        print("No moves")
        return

    (fr, fc, tr, tc) = best_move
    if abs(tr - fr) > 1:
        if my_color == "white":
            captured_black += 1
        if my_color == "black":
            captured_white += 1

    print(f"Bot ({my_color}) do: {best_move}")

    request = {
        "type": "move",
        "content": {
            "gameId": game_id,
            "move": {"fromRow": fr, "fromCol": fc, "toRow": tr, "toCol": tc}
        }
    }
    ws_app.send(json.dumps(request))


def on_open(ws_app):
    message = {"type": "joinQueue"}
    ws_app.send(json.dumps(message))


def on_error(ws_app, error):
    print("Error occured:", error)


def on_close(ws_app, close_status_code, close_msg):
    print("Connection closed")


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", type=str, help="Pass address of the server here")
    args = parser.parse_args()
    if args.url:
        ws_app = websocket.WebSocketApp(
            args.url,
            on_message=on_message,
            on_open=on_open,
            on_error=on_error,
            on_close=on_close,
        )
    else:
        ws_app = websocket.WebSocketApp(
            URL,
            on_message=on_message,
            on_open=on_open,
            on_error=on_error,
            on_close=on_close,
        )
    ws_thread = threading.Thread(target=ws_app.run_forever)
    ws_thread.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        ws_app.close()
        ws_thread.join()


if __name__ == "__main__":
    main(sys.argv)
