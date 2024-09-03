import json
import os
import re
import subprocess
import time

from datetime import datetime
from common import (project_root)

# Variables ============================================================================================================
gradle_properties_file = os.path.join(project_root, "gradle.properties")
read_me_file = os.path.join(project_root, "README.md")
files_with_version_to_change = [gradle_properties_file, read_me_file]

api_directory = 'lib/src/main/kotlin/com/lemonappdev/konsist/api'

# Methods ==============================================================================================================
def choose_release_option():
    """
    Prompts the user to choose between "Main Release - Upgrade Minor" and "Hotfix Release - Upgrade Patch".

    Returns: The chosen option (1 or 2).
    """

    print(f"\033[32mWhich release option do you choose? Write:\033[0m")
    print(f"\033[35m1 - if you want to create \"Main Release - Upgrade Minor\"\033[0m")
    print(f"\033[34m2 - if you want to create \"Hotfix Release - Upgrade Patch\"\033[0m")

    while True:
        choice = input(f"\033[31;1mEnter your choice (1 or 2): \033[0m")
        if choice in ["1", "2"]:
            return int(choice)
        else:
            print(f"\033[31mInvalid choice. Please enter 1 or 2.\033[0m")


def get_old_konsist_version():
    """
    Reads the gradle.properties file and returns the value of the `konsist.version` property.

    Returns: The value of the `konsist.version` property or None if not found.
    """

    gradle_properties_file = os.path.join(project_root, "gradle.properties")

    try:
        with open(gradle_properties_file, 'r') as f:
            for line in f:
                line = line.strip()
                if line.startswith('konsist.version='):
                    return line.split('=')[1]
        raise ValueError("konsist.version property not found in gradle.properties")
    except FileNotFoundError:
        print(f"Error: Gradle properties file '{gradle_properties_file}' not found.")
    except Exception as e:
        print(f"Error: An unexpected error occurred: {e}")
    return None


def get_new_konsist_version(release_option_num, old_version):
    """
    Calculates the new version based on the release option and old version.

    Args:
        release_option_num: The chosen release option number (1 or 2).
        old_version: The current version string (obtained from get_old_konsist_version).

    Returns: The new version string or None if invalid option.
    """

    if not old_version:
        print("Error: Unable to determine old version.")
        return None

    major_version, minor_version, patch_version = old_version.split('.')

    if release_option_num == 1:
        new_version = f"{major_version}.{int(minor_version) + 1}.0"
    elif release_option_num == 2:
        new_version = f"{major_version}.{minor_version}.{int(patch_version) + 1}"
    else:
        print(f"Error: Invalid release option number: {release_option_num}")
        return None

    return new_version


def change_branch_and_merge():
    """
    Changes branch to 'development', fetches and pulls changes and merges 'main' into 'develop'.
    """

    try:
        # Change branch to 'development'
        subprocess.run(["git", "checkout", "development"], check=True)
        print("Switched to branch 'development'")

        # Fetch and pull changes
        subprocess.run(["git", "fetch"], check=True)
        print("Fetched changes")
        subprocess.run(["git", "pull"], check=True)
        print("Pulled changes")

        # Merge 'main' into 'develop'
        subprocess.run(["git", "merge", "main"], check=True)
        print("Merged 'main' into 'develop'")

    except subprocess.CalledProcessError as e:
        print(f"Error: {e}")


def check_for_uncommitted_changes():
    """
    Checks if there are uncommitted changes in the current Git repository.

    Returns: True if there are uncommitted changes, False otherwise.
    """

    result = subprocess.run(["git", "status", "--porcelain"], check=True, capture_output=True)
    return bool(result.stdout.strip())


def create_release_branch(version):
    """
    Checks if a release branch with the specified version exists. If not, creates it from the 'development' branch.

    Args: version: The version number to check for.

    Returns: Branch title
    """

    try:
        # Check if the release branch already exists
        result = subprocess.run(["git", "branch", "--list"], check=True, capture_output=True)
        if f"Release/v{version}" in result.stdout.decode():
            print(f"Release branch 'release/v{version}' already exists.")

            # Switch to the existing branch
            subprocess.run(["git", "checkout", f"release/v{version}"], check=True)
            return

        # Create the release branch from 'development'
        branch_title = f"release/v{version}"
        subprocess.run(["git", "checkout", "-b", branch_title], check=True)
        print(f"Created release branch '{branch_title}'")
        return branch_title

    except subprocess.CalledProcessError as e:
        print(f"Error: {e}")
        return None

def replace_konsist_version(old_version, new_version, files):
    """
    Replaces all occurrences of `old_version` with `new_version` in the provided files.

    Args:
      old_version: The old version string.
      new_version: The new version string.
      files: A list of file paths to modify.
    """

    for file_path in files:
        with open(file_path, 'r') as f:
            file_text = f.read()
            file_text = file_text.replace(old_version, new_version)

        with open(file_path, 'w') as f:
            f.write(file_text)
            print(f"Updated version in: {file_path}")

    commit_message = f"Replace Konsist version {old_version} to {new_version}"
    subprocess.run(["git", "add", "."], check=True)  # Stage all changes
    subprocess.run(["git", "commit", "-m", commit_message], check=True)  # Commit changes

def find_files_with_deprecated_annotation(directory, version):
    """
    Finds Kotlin files containing the @Deprecated annotation with the specified pattern.

    Args: directory: The directory to search.

    Returns: A list of file paths that match the criteria.
    """

    files_with_deprecated_annotation = []
    pattern = fr'@Deprecated\(".*{version}'

    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.kt'):
                file_path = os.path.join(root, file)
                with open(file_path, 'r') as f:
                    text = f.read()
                    if re.search(pattern, text):
                        files_with_deprecated_annotation.append(file_path)

    return  files_with_deprecated_annotation

def display_clickable_file_paths(file_path):
    # Construct the hyperlink URL
    hyperlink_url = f"file://{os.path.abspath(file_path)}"

    # Create the colored hyperlink text
    hyperlink_text = f"\033[34m\033]8;;{hyperlink_url}\033\\{file_path}\033]8;;\033\\\033[0m"

    print(hyperlink_text)

def create_pull_request_to_main(version):
    """
    Creates a pull request to the main branch with the specified title.

    Args:
      version: The version number to include in the title.
    """

    try:
        # Push the current branch to the remote repository, setting the upstream branch if needed
        subprocess.run(["git", "push", "--set-upstream", "origin", "HEAD"], check=True)

        # Create the pull request using the GitHub CLI
        subprocess.run(["gh", "pr", "create", "--title", f"Release/v{version}", "--body", "",  "--base", "main"], check=True)

    except subprocess.CalledProcessError as e:
        print(f"Error: {e}")

def get_latest_commit_sha(branch):
    """
    Get the latest commit SHA from a specific branch using GitHub CLI.
    """
    try:
        result = subprocess.run(
            ['gh', 'api', f'/repos/nataliapeterwas/konsist/commits/{branch}', '--jq', '.sha'],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )

        if result.returncode != 0:
            print(f"Error fetching latest commit SHA: {result.stderr}")
            return None

        return result.stdout.strip()

    except Exception as e:
        print(f"An error occurred while getting the latest commit SHA: {e}")
        return None

def check_github_checks(ref):
    """
    Check the status of GitHub checks for a specific commit.
    """
    try:
        result = subprocess.run(
            ['gh', 'api', f'/repos/nataliapeterwas/konsist/commits/{ref}/check-runs', '--jq', '.check_runs'], # change to LemonAppDev!!!
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )

        if result.returncode != 0:
            print("Error fetching check runs:", result.stderr)
            return None

        check_runs = json.loads(result.stdout)

        # Flag to track if all checks passed (excluding skipped)
        # -1 - failed, 1 - success, 0 - running or neutral
        statuses = []

        for check in check_runs:
            check_name = check['name']
            check_status = check['conclusion']
            check_status_text = check.get('status', '')  # Check the status field for queued, in progress, etc.

            if check_status == 'success':
                print(f"{datetime.now().strftime('%H:%M:%S')}: Check '{check_name}' passed.")
                statuses.append(1)
            elif check_status == 'failure':
                print(f"{datetime.now().strftime('%H:%M:%S')}: Check '{check_name}' failed.")
                statuses.append(-1)
            elif check_status_text == 'queued':
                print(f"{datetime.now().strftime('%H:%M:%S')}: Check '{check_name}' is queued and waiting to run.")
                statuses.append(0)
            elif check_status_text == 'in_progress':
                print(f"{datetime.now().strftime('%H:%M:%S')}: Check '{check_name}' is currently running.")
                statuses.append(0)
            elif check_status == 'neutral':
                print(f"{datetime.now().strftime('%H:%M:%S')}: Check '{check_name}' skipped.")
                statuses.append(0)
            else:
                print(f"{datetime.now().strftime('%H:%M:%S')}: Check '{check_name}' status: {check_status_text}")

        return statuses

    except Exception as e:
        print(f"An error occurred while checking the GitHub checks: {e}")
        return None


def create_release():
    chosen_option = 1  # remove!!!
    #
    # # chosen_option = choose_release_option()
    # print(f"You chose option: {chosen_option}")
    #
    old_konsist_version = get_old_konsist_version()
    print(f"Old konsist version: {old_konsist_version}")

    # Check if old version is None
    if old_konsist_version is None:
        print("Error: Unable to determine old version from `gradle.properties`.")
        return

    new_konsist_version = get_new_konsist_version(chosen_option, old_konsist_version)
    print(f"New konsist version: {new_konsist_version}")

    # Check if new version is None
    if new_konsist_version is None:
        print("Error: Unable to determine new version.")
        return

    change_branch_and_merge()

    if check_for_uncommitted_changes():
        print("Error: There are uncommitted changes. Please commit or stash them before merging.")
        return
    else:
        print("There are no uncommitted changes. Script continues...")

    release_branch_title = create_release_branch(new_konsist_version)

    replace_konsist_version(old_konsist_version, new_konsist_version, files_with_version_to_change)

    deprecated_files = find_files_with_deprecated_annotation(api_directory, new_konsist_version)

    # Check if list of files with deprecated annotation is not empty
    if deprecated_files:
        print(f"Files contains @Deprecated annotation with {new_konsist_version} version:")
        for file in deprecated_files:
            file_path = os.path.join(project_root, file)
            display_clickable_file_paths(file_path)
        print(f"Remove deprecated declarations in the above files.")
        return
    else:
        print(f"No files contains @Deprecated annotation with {new_konsist_version} version.")

    create_pull_request_to_main(new_konsist_version)

    while True:
        # Get latest commit SHA
        latest_commit_sha = get_latest_commit_sha(release_branch_title)
        # latest_commit_sha = get_latest_commit_sha("release/v0.17.0") # remove this !!!
        print(f"Latest commit SHA: {latest_commit_sha}")

        if not latest_commit_sha:
            print(f"{datetime.now().strftime('%H:%M:%S')}: Error fetching commit SHA.")
            break

        # Check GitHub checks
        check_statuses = check_github_checks(latest_commit_sha)

        # Determine the status of the checks
        if -1 in check_statuses:
            print(f"{datetime.now().strftime('%H:%M:%S')}: A check failed. Exiting script.")
            break

        if 0 in check_statuses:
            print(f"{datetime.now().strftime('%H:%M:%S')}: Checks in progress. Waiting for re-run.")
            time.sleep(60)  # Wait a minute before checking again
            continue

        if all(status == 1 for status in check_statuses):
            print(f"{datetime.now().strftime('%H:%M:%S')}: All checks passed. Continuing script execution.")
            # Add your script logic here
            break  # Exit the loop if all checks passed


# Script ===============================================================================================================
create_release()
