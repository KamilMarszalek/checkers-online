from fastapi import FastAPI
import uuid
from bot.bot_session import BotSession
import asyncio

URL = "ws://localhost:8080/ws"
app = FastAPI()

active_sessions = {}

"""
    To run app go to backend directory
    and type in the terminal:
        uvicorn bot.server:app
"""


@app.get("/play")
async def play():
    session_id = str(uuid.uuid4())
    session = BotSession(url=URL)
    active_sessions[session_id] = session

    asyncio.create_task(session.run())

    return {"status": "bot started", "session_id": session_id}
