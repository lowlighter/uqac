# Dépendances
import math
import time
import chess
import random
import berserk

# Cartes d'utilités
u_type = [None, 100, 280, 320, 479, 929, 60000]
u_position = [None, [0, 0, 0, 0, 0, 0, 0, 0, -31, 8, -7, -37, -36, -14, 3, -31, -22, 9, 5, -11, -10, -2, 3, -19, -26, 3, 10, 9, 6, 1, 0, -23, -17, 16, -2, 15, 14, 0, 15, -13, 7, 29, 21, 44, 40, 31, 44, 7, 78, 83, 86, 73, 102, 82, 85, 90, 0, 0, 0, 0, 0, 0, 0, 0], [-74, -23, -26, -24, -19, -35, -22, -69, -23, -15, 2, 0, 2, 0, -23, -20, -18, 10, 13, 22, 18, 15, 11, -14, -1, 5, 31, 21, 22, 35, 2, 0, 24, 24, 45, 37, 33, 41, 25, 17, 10, 67, 1, 74, 73, 27, 62, -2, -3, -6, 100, -36, 4, 62, -4, -14, -66, -53, -75, -75, -10, -55, -58, -70], [-7, 2, -15, -12, -14, -15, -10, -10, 19, 20, 11, 6, 7, 6, 20, 16, 14, 25, 24, 15, 8, 25, 20, 15, 13, 10, 17, 23, 17, 16, 0, 7, 25, 17, 20, 34, 26, 25, 15, 10, -9, 39, -32, 41, 52, -10, 28, -14, -11, 20, 35, -42, -39, 31, 2, -22, -59, -78, -82, -76, -23, -107, -37, -50], [-30, -24, -18, 5, -2, -18, -31, -32, -53, -38, -31, -26, -29, -43, -44, -43, -42, -28, -42, -25, -25, -35, -26, -46, -28, -35, -16, -21, -13, -29, -46, -30, 0, 5, 16, 13, 18, -4, -9, -6, 19, 35, 28, 33, 45, 27, 25, 15, 55, 29, 56, 67, 55, 62, 34, 60, 35, 29, 33, 4, 37, 33, 56, 50], [-39, -30, -31, -13, -31, -36, -34, -42, -36, -18, 0, -19, -15, -15, -21, -38, -30, -6, -13, -11, -16, -11, -16, -27, -14, -15, -2, -5, -1, -10, -20, -22, 1, -16, 22, 17, 25, 20, -13, -6, -2, 43, 32, 60, 72, 63, 43, 2, 14, 32, 60, -10, 20, 76, 57, 24, 6, 1, -8, -104, 69, 24, 88, 26], [17, 30, -3, -14, 6, -1, 40, 18, -4, 3, -14, -50, -57, -18, 13, 4, -47, -42, -43, -79, -64, -32, -29, -32, -55, -43, -52, -28, -51, -47, -8, -50, -55, 50, 11, -4, -19, 13, 0, -49, -62, 12, -57, 44, -67, 28, 37, -31, -32, 10, 55, 56, 56, 55, 10, 3, 4, 54, 47, -99, -99, 60, 83, -62]]

# Indique si l'état est terminal
def terminal(state):
  return state.is_checkmate() or state.is_stalemate()

# Retourne le score d'utilité de l'état actuel
def utility(state):
  u = 0
  for s, p in state.piece_map().items():
    if p.color == chess.WHITE:
      u += u_type[p.piece_type] + u_position[p.piece_type][s]
    else:
      u -= u_type[p.piece_type] + u_position[p.piece_type][s]
  return u

# Génère les successeurs de l'état actuel
def successors(state):
  states = []
  for m in state.legal_moves:
    s = state.copy()
    s.push(m)
    states.append([m, s])
  #random.shuffle(states)
  return states
    
# Minimax + alpha-beta (debug)
def alphabeta_debug(state, d):
  t = time.time()
  m = alphabeta(state, d)
  print(">>> "+str(m)+" ("+str(time.time() - t)+" sec)")
  return m
    
# Minimax + alpha-beta
def alphabeta(state, d = math.inf, a = -math.inf, b = +math.inf):
  best = None
  if terminal(state): return best
  v = -math.inf
  for m, s in successors(state):
    mv = minvalue(s, a, b, d)
    v = max(v, mv)
    if v > a: best = m
    a = max(a, v)
  return best
    
# Max-value
def maxvalue(state, a, b, d):
  if terminal(state) or d < 0: return utility(state)
  v = -math.inf
  for m, s in successors(state):
    v = max(v, minvalue(s, a, b, d-1))
    if v >= b: return v
    a = max(a, v)
  return v
  
# Min-value
def minvalue(state, a, b, d):
  if terminal(state) or d < 0: return utility(state)
  v = +math.inf
  for m, s in successors(state):
    v = min(v, maxvalue(s, a, b, d-1))
    if v <= a: return v
    b = min(b, v)
  return v

# Session sur lichess
game = "i6d8OoaX"
session = berserk.TokenSession("TMpilfQJ6prfuhVu")
lichess = berserk.Client(session)
board = chess.Board()

# Récupération des événements
for response in lichess.bots.stream_game_state(game):
    # Initialisation de la partie
    if (response["type"] == "gameFull"):
      if len(response["state"]["moves"]):
        for move in response["state"]["moves"].split(" "):
          board.push(chess.Move.from_uci(move))
     
    # Mise à jour de la partie
    if (response["type"] == "gameState"):
      move = response["moves"].split(" ")[-1]
      if (board.turn == chess.BLACK): print("<<< "+move)
      board.push(chess.Move.from_uci(move))

    # Coup suivant
    if (board.turn == chess.WHITE):
      lichess.bots.make_move(game, alphabeta_debug(board, 2))

    # Partie terminée
    if board.is_game_over():
      print("==> Partie terminée")
