#!/usr/bin/env python2

import subprocess
import sys


def get_git_root():
    return subprocess.check_output(["git", "rev-parse", "--show-toplevel"]).\
        strip()


def run_checkstyle(jar_path, checks_path, suppressions_path, xpath_suppressions_path, files):
    command = [
        "java",
        "-DsuppressionFile={}".format(suppressions_path),
        "-DsuppressionXpathFile={}".format(xpath_suppressions_path),
        "-jar",
        jar_path,
        "-c",
        checks_path
    ]

    command.extend(files)

    process = subprocess.Popen(command, stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE)
    status = process.wait()

    if status == 0:
        return []

    stdout, stderr = process.communicate()
    line_list = stdout.strip().split("\n")
    line_list = [line for line in line_list if "Starting audit" not in line]
    line_list = [line for line in line_list if "Audit done" not in line]
    line_list = [line for line in line_list if "Checkstyle ends" not in line]

    return line_list


def lint(files):
    root = get_git_root()

    results = run_checkstyle(
        root + "/linters/checkstyle/checkstyle-8.45.1-all.jar",
        root + "/linters/checkstyle/config.xml",
        root + "/linters/checkstyle/suppressions.xml",
        root + "/linters/checkstyle/suppressions-xpath.xml",
        files
    )

    for result in results:
        print result

    return len(results) == 0


if __name__ == "__main__":
    if not lint(sys.argv[1:]):
        sys.exit(1)
