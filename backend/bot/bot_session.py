import websockets
import json
from bot import Bot
import asyncio
from typing import Optional, Any


class BotSession:
    """
    Manages a session for a bot connecting via WebSocket.

    Attributes:
        url (str): The WebSocket URL to connect.
        bot (Optional[Bot]): Bot instance initialized upon game creation.
        my_color (Optional[str]): Assigned color for this bot ("white" or "black").
        game_id (Optional[Any]): The identifier for the game.
        captured_white (int): Count of captured white pieces.
        captured_black (int): Count of captured black pieces.
        ws (Optional[websockets.WebSocketClientProtocol]): The active WebSocket connection.
    """

    def __init__(self, url: str) -> None:
        """
        Initializes the BotSession with a URL.

        Args:
            url (str): The WebSocket URL to connect to.
        """
        self.url: str = url
        self.bot: Optional[Bot] = None
        self.my_color: Optional[str] = None
        self.game_id: Optional[Any] = None
        self.captured_white: int = 0
        self.captured_black: int = 0

        self.ws: Optional[websockets.WebSocketClientProtocol] = None

    async def run(self) -> None:
        """
        Connects to the WebSocket server and processes incoming messages.
        Sends an initial joinQueue message and enters a loop to handle messages.
        """
        print("BotSession connecting to: ", self.url)
        try:
            async with websockets.connect(self.url) as ws:
                self.ws = ws

                await self.ws.send(
                    json.dumps(
                        {
                            "type": "joinQueue",
                            "content": {
                                "user": {
                                    "username": "Herkules"
                                }
                            }
                        }
                    )
                )
                print("Sent joinQueue message")

                while True:
                    try:
                        message = await self.ws.recv()
                    except websockets.exceptions.ConnectionClosed:
                        print("Connection closed by server")
                        break

                    if not message:
                        continue

                    await self.handle_message(message)
        except Exception as e:
            print(f"Exception in bot session: {e}")

        print("BotSession finished")

    async def handle_message(self, raw_message: str) -> None:
        """
        Handles a raw JSON message string from the server.

        Parses the message and takes appropriate action based on its type.

        Args:
            raw_message (str): The JSON message as a string.
        """
        dict_message = json.loads(raw_message)
        msg_type = dict_message.get("type")
        print("Received:", dict_message)

        if msg_type == "Game created":
            self.game_id = dict_message["content"]["gameId"]
            self.my_color = dict_message["content"]["color"]
            print(f"Assigned color:  {self.my_color}, game_id: {self.game_id}")

            self.bot = Bot(self.my_color)

            if self.my_color == "white":
                self.bot.current_player = "white"
                await self.do_bot_move()
            else:
                self.bot.current_player = "white"

        elif msg_type == "move":
            content = dict_message.get("content")
            if not content:
                print("Received 'move' with empty content - ignore")
                return

            move_data = content.get("move")
            if not move_data:
                print("Move event without 'move' field - ignore")
                return

            fr = move_data["fromRow"]
            fc = move_data["fromCol"]
            tr = move_data["toRow"]
            tc = move_data["toCol"]
            self.bot.board = self.bot.make_local_move(self.bot.board, (fr, fc, tr, tc))

            self.bot.current_player = content["currentTurn"]

            if (self.my_color == "white" and self.bot.current_player == "white") or (
                    self.my_color == "black" and self.bot.current_player == "black"
            ):
                await self.do_bot_move()

        elif msg_type == "gameEnd":
            result = dict_message["content"]["result"]
            print(f"End of the game! Result: {result}")
            print("Captured black:", self.captured_black)
            print("Captured white:", self.captured_white)

            if self.ws:
                await self.ws.close()
        elif msg_type == "waiting":
            print("Waiting for an opponent...")
        else:
            print("Unknown message type:", msg_type)

    async def do_bot_move(self) -> None:
        """
        Chooses the best move for the bot and sends it to the server.

        Increments capture counters if the move is a jump.
        """
        if not self.bot:
            return

        best_move = await asyncio.to_thread(self.bot.choose_best_move, depth=6)
        if best_move is None:
            print("No moves available")
            for row in self.bot.board:
                row_str = ""
                for col in row:
                    if col is None:
                        row_str += "-"
                    else:
                        row_str += col
                print(row_str)
            return

        (fr, fc, tr, tc) = best_move
        if abs(tr - fr) > 1:
            if self.my_color == "white":
                self.captured_black += 1
            else:
                self.captured_white += 1

        print(f"Bot ({self.my_color}) do move: {best_move}")

        request = {
            "type": "move",
            "content": {
                "gameId": self.game_id,
                "move": {
                    "fromRow": fr,
                    "fromCol": fc,
                    "toRow": tr,
                    "toCol": tc,
                },
            },
        }
        if self.ws:
            await self.ws.send(json.dumps(request))
