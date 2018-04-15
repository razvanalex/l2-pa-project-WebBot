from os import path, makedirs, walk, listdir, chmod, chdir
from pprint import pprint as pp
from subprocess import call, check_output, check_call, CalledProcessError
from shutil import copyfile, rmtree
import argparse
import sys


def run_system_command(cmd, err_msg=None):

    sys.stdout.write("System cmd: {}\n".format(cmd))

    rc = call([cmd], shell=True)
    if err_msg and rc:
        sys.stdout.write(err_msg)
        exit(rc)

    return rc


def create_archive(args):

    if not path.exists(args.makefile):
        sys.stdout.write("Path makefile invalid.\n")
        exit(1)

    if not path.exists(args.readme):
        sys.stdout.write("Path readme invalid.\n")
        exit(1)

    for file in args.files:
        if not path.exists(file):
            sys.stdout.write("Path fisier invalid {}.\n".format(file))
            exit(1)

    repo_path = check_output(["git", "rev-parse", "--show-toplevel"]).decode("utf-8")
    team_name = repo_path.strip().split("/")[-1].split("-")[-1]

    archive_name = "./{}{}_etapa{}.zip".format(args.seria, team_name, args.etapa)

    run_system_command("zip {} {} {} {}".format(archive_name, args.makefile, args.readme, " ".join(args.files)))

    if not path.exists(archive_name):
        sys.stdout.write("Eroare: Arhiva nu a fost creata!\n")
        exit(0)

    sys.stdout.write("Arhiva a fost creata cu succes!\n")
    return archive_name


def upload_archive(args):

    run_system_command("git status")

    run_system_command("git add {}".format(args.archive),
                       "Eroare: Arhiva nu a putut fi adaugata. Verificati ca ati sincronizat "
                       "toate modificarile pe git.\n")

    run_system_command("git commit -m \"Upload solutie pentru etapa {}\"".format(args.etapa),
                       "Eroare: Comanda de commit a esuat. Este posibil ca arhiva sa fi fost salvata deja pe git.\n")

    run_system_command("git push",
                       "Eroare: Comanda push a esuat. Verificati ca ati sincronizat "
                       "toate modificarile pe git.\n")

    sys.stdout.write("Succes: Arhiva a fost salvata!\n")
    return 0


def remove_old_arena(args):
    if path.exists(args.arena):
        rmtree(args.arena)

def test_archive(args):

    args.arena = "./tmp_arena"

    remove_old_arena(args)
    makedirs(args.arena)

    run_system_command("cp {} {}/{}".format(args.archive, args.arena, args.archive))
    chdir(args.arena)
    run_system_command("git clone https://gitlab.cs.pub.ro/pa-assignments/halite-II-resources.git")
    chdir("halite-II-resources")
    run_system_command("unzip -o ../{}".format(args.archive))
    run_system_command("python ./run.py --cmd \"{}\" --round {}".format(args.test, args.etapa))
    chdir("../../")
    remove_old_arena(args)


def main():

    parser = argparse.ArgumentParser(description='PA - Salveaza arhiva in branch-ul corespunzator')
    parser.add_argument('--etapa', required=True, help="Id-ul etapei curente. (e.g. 1, 2, 3, 4)")
    parser.add_argument('--seria', required=True, help="Seria echipei voastre, (e.g. CC")
    parser.add_argument('--makefile', required=True, help="Path spre fisierul Makefile (sau CMakeLists.txt)")
    parser.add_argument('--readme', required=True, help="Path spre fisierul Readme corespunzator. "
                                                        "Atentie: Acesta trebuie sa fie un simplu fisier text (nu .docx, etc.)")
    parser.add_argument('--files', nargs="+", required=True, help="Path spre sursele necesare compilarii/rularii botului.")
    parser.add_argument('--upload', action="store_true", help="(Optional) Adauga arhiva pe git, in branch-ul curent.")
    parser.add_argument('--test', help="(Optional): Comanda de rulare a botului (eg. java MyBot). "
                                       "\nDaca primeste acest parametru, scriptul testeaza arhiva\n"
                                       "folosind scriptul oficial de evaluare.\n")

    args = parser.parse_args()

    if check_output(["git", "rev-parse", "--is-inside-work-tree"]).decode("utf-8") != "true\n":
        sys.stdout.write("Scriptul trebuie rulat din repository-ul vostru de git.\n")
        exit(1)

    if args.upload:
        run_system_command("git diff-index --quiet $(git write-tree)",
                           "\nEroare: Inainte de a face upload, asigurati-va "
                           "ca ati sincronizat toate modificarile pe git.\n")

    args.archive = create_archive(args)

    if args.test and len(args.test):
        test_archive(args)

    if args.upload:
        upload_archive(args)


if __name__ == "__main__":
    main()

