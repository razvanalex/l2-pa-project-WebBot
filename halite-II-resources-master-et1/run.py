import os
import sys
import json
import argparse
import math
import platform

from subprocess import call
from pprint import pprint as pp


def produce_game_environment():

    sys.stdout.write("Compiling game engine..\n")
    cmd = "cd ./environment; cmake .; make -j 4; cd ../; cp ./environment/halite ./halite"
    call([cmd], shell=True)

    if not os.path.exists("halite"):
        sys.stderr.write("Failed to produce executable environment\n")
        sys.stderr.write("Corrupt archive?")
        exit(1)


def prepare_env():

    produce_game_environment()
    if os.path.exists("makefile") or os.path.exists("Makefile"):
        sys.stdout.write("Compiling player sources..\n")
        call(["make"], shell=True)

    call(["rm -rf *.hlt; rm -rf replays/*; rm -rf replays-readble/*; mkdir -p replays;"], shell=True)


class HaliteEnv(object):

    def __init__(self,
                 player_bot_cmd,
                 height=30,
                 width=30,
                 seed=42,
                 max_turns=-1):
        self.bots      = [player_bot_cmd]
        self.height    = height
        self.width     = width
        self.seed      = seed
        self.max_turns = max_turns

    def __add_map(self, cmd):
        cmd += "-d \"{0} {1}\" ".format(self.width, self.height)
        cmd += "-s {0}".format(self.seed)
        return cmd

    def __add_bot(self, cmd, bot_cmd):
        cmd += " \"{0}\"".format(bot_cmd)
        return cmd

    def __add_turn_limit(self, cmd):
        if self.max_turns > 0:
            cmd += " --max_turns {0} ".format(self.max_turns)
        return cmd

    def run(self):
        sys.stdout.write("Map: Height {0}, Width {1}, Seed {2}\n".format(self.height, self.width, self.seed))

        cmd = "./halite -q -z "
        cmd = self.__add_map(cmd)
        # cmd = self.__add_turn_limit(cmd)

        for bot in self.bots:
            cmd = self.__add_bot(cmd, bot)

        # pp(cmd)
        call([cmd], shell=True)

        binary_res, text_res = None, None

        for file in os.listdir("./"):
            if file.endswith(".hlt"):
                binary_res = file
            elif file.endswith(".hlt.json"):
                text_res = file

        if not text_res:
            sys.stderr.write("There was an error during the game, "
                             "no valid replay file was produced!\n")
            return None

        return binary_res, text_res


def default_map_limit(height, width):
    return int(math.sqrt(height * width) * 10)


def compute_score(num_frames, soft_limit, hard_limit, game_weight):

    if num_frames <= soft_limit:
        return game_weight

    if num_frames >= hard_limit:
        return 0.0

    return game_weight * (1 - (num_frames - soft_limit) / (hard_limit - soft_limit))


def round_one(cmd):

    sys.stdout.write("Round 1 - single player challenge!\n")

    env   = HaliteEnv(cmd)
    games = [
        (384, 256, 3583588908, 130, 160),
        (384, 256, 1673031865, 105, 135),
        (384, 256, 1773807367, 100, 130),
        (288, 192, 1942373999, 85, 115),
        (288, 192, 142342898, 90, 130)
    ]

    max_score    = 0.45                    # Round score
    game_weight  = max_score / len(games)  # Equal weight / game
    player_score = 0.0

    for idx, game in enumerate(games):

        width, height, seed, soft_limit, hard_limit = game

        env.height = height
        env.width  = width
        env.seed   = seed
        points     = 0.0

        binary_log, text_log = env.run()

        if text_log is None:
            sys.stdout.write("Map {} score: {}\n".format(idx, points))
            continue

        else:
            with open(text_log, "r") as f:

                result = json.loads(f.read())

                map_conquered = result["destroyed_planets"] == 0 and result["free_planets"] == 0
                num_frames    = result["num_frames"]

                if map_conquered:
                    sys.stdout.write("Map conquered in {}!\n".format(result["num_frames"]))

                    points = compute_score(float(num_frames),
                                           float(soft_limit),
                                           float(hard_limit),
                                           game_weight * 0.8)

                    if not result["self-collisions"]:
                        points += game_weight * 0.2
                    else:
                        sys.stdout.write("Self collisions detected!\n")

                else:
                    sys.stdout.write("Failure to occupy all planets!\n")
                    if result["destroyed_planets"] > 0:
                        sys.stdout.write("{} planets were destroyed!\n".format(result["destroyed_planets"]))
                    if result["free_planets"] > 0:
                        sys.stdout.write("{}/{} planets were left unoccupied.\n".format(result["free_planets"],
                                                                                      result["occupied_planets"]))
                    points = 0.0

                sys.stdout.write("Map score: {}\n".format(points))
                player_score += points

        call(["mv {} ./replays/replay-map-{}.hlt".format(binary_log, idx)], shell=True)
        call(["mv {} ./replays-readable/replay-map-{}.hlt".format(text_log, idx)], shell=True)

    final_score = round(min(player_score, max_score), 2)

    sys.stdout.write("Round 1 - done!\nFinal score: {}/{}\n".format(final_score,
                                                                    max_score))

def round_two(cmd):

    sys.stdout.write("Round 2 - 1vs1 battles!\n")
    sys.stdout.write("Coming soon!")

def round_three(cmd):

    sys.stdout.write("Round 2 - 4 player battles!\n")
    sys.stdout.write("Coming soon!")


def cleanup():
    call(["rm -f *.hlt; rm -rf replays/*.hlt; rm -rf replays-readable/*.hlt; rm -f *.log"], shell=True)
    if os.path.exists("makefile") or os.path.exists("Makefile"):
        call(["make clean"], shell=True)


def main():

    parser = argparse.ArgumentParser(description='PA project evaluator')
    parser.add_argument('--cmd', required=True, help="Command line instruction to execute the bot. eg: ./MyBot")
    parser.add_argument('--round', type=int, default=1, help="Round index (1, 2, or 3), default 1")
    parser.add_argument('--clean', action="store_true", help="Remove logs/game results, call make clean")

    args = parser.parse_args()
    prepare_env()
    rounds = [round_one, round_two, round_three]
    if args.round < 1 or args.round > len(rounds):
        sys.stderr.write("Invalid round parameter (should be an integer in [1, 3])\n")
        exit(1)

    # Let the games begin!
    rounds[args.round - 1](args.cmd)
    if args.clean:
        cleanup()


if __name__ == "__main__":
    main()
