package pw.checkers.game.data.dto.message

object MessageType {
    object Incoming {
        const val GAME_CREATED = "gameCreated"
        const val MOVE = "move"
        const val POSSIBILITIES = "possibilities"
        const val WAITING = "waiting"
        const val GAME_ENDING = "gameEnd"
        const val REJECTION = "rejection"
        const val REMATCH_REQUEST = "rematchRequest"
    }

    object Outgoing {
        const val JOIN_QUEUE = "joinQueue"
        const val LEAVE_QUEUE = "leaveQueue"
        const val MOVE = "move"
        const val POSSIBILITIES = "possibilities"
        const val REMATCH_REQUEST = "rematchRequest"
        const val ACCEPT_REMATCH = "acceptRematch"
        const val DECLINE_REMATCH = "declineRematch"
        const val LEAVE = "leave"
    }
}
