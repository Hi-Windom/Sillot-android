import os
import re
from argparse import ArgumentParser
from collections import defaultdict

import github

# ensure the milestone is open before run this
docmap = {
    "Feature": "引入特性",
    "Enhancement": "改进功能",
    "Bug": "修复错误",
    "Securiy": "修补漏洞",
    "Document": "文档相关",
    "Refactor": "开发重构",
    "Abolishment": "移除功能"
}


def generate_msg_from_repo(repo_name, tag_name):
    """Generate changelog messages from repository and tag name.

    Envs:
        GITHUB_HOST: the custom github host.
        GITHUB_TOKEN: the github access token.

    Args:
        repo_name (str): The repository name
        tag_name (str): the tag name
    """
    hostname = os.getenv("GITHUB_HOST") or "api.github.com"
    token = os.getenv("GITHUB_TOKEN")
    desc_mapping = defaultdict(list)

    gh = github.Github(token, base_url=f"https://{hostname}")
    repo = gh.get_repo(repo_name)
    milestone = find_milestone(repo, tag_name)

    for issue in repo.get_issues(state="closed", milestone=milestone):
        # REF https://pygithub.readthedocs.io/en/latest/github_objects/Issue.html#github.Issue.Issue
        desc_mapping[get_issue_first_label(issue)].append(
            {"title": issue.title, "url": issue.html_url}
        )
    generate_msg(desc_mapping)


def find_milestone(repo, title):
    """Find the milestone in a repository that is similar to milestone title

    Args:
        repo (github.repository.Repository): The repository to search
        title (str): the title to match

    Returns:
        The milestone which title matches the given argument.
        If no milestone matches, it will return None
    """
    thisRelease = title.split("/")[-1]
    pat = re.search("v([0-9.]+)", thisRelease)
    if not pat:
        return None
    version = ".".join(pat.group(1).split(".")[:2])
    print(f'''
---
## https://github.com/Hi-Windom/Sillot/releases/tag/{thisRelease}

⚠️ 这是自动构建的开发者版本！数据无价，请勿用于生产环节
❤️ 欢迎共建汐洛 694357845@qq.com
🚧 [Sillot is currently in active development](https://github.com/orgs/Hi-Windom/projects/2/views/2)

📱 [蒲公英内测apk代理下载](https://www.pgyer.com/sillot)
<span>
<img src="https://img.shields.io/badge/Android 11+-black?logo=android" title=""/>
</span>

---

''')
    for milestone in repo.get_milestones():
        if version in milestone.title:
            return milestone


def get_issue_first_label(issue):
    """Get the first label from issue, if no labels, return empty string."""
    for label in issue.get_labels():
        if label.name in docmap:
            return label.name
    return ""


def generate_msg(desc_mapping):
    """Print changelogs from direction."""
    print()
    print('## [@Sillot](https://github.com/Hi-Windom/Sillot)\n')
    for header in docmap:
        if not desc_mapping[header]:
            continue
        print(f"### {docmap[header]}\n")
        for item in desc_mapping[header]:
            print(f"* [{item['title']}]({item['url']})")
        print()


if __name__ == "__main__":
    parser = ArgumentParser(
        description="Automaticly generate information from issues by tag."
    )
    parser.add_argument("-t", "--tag", help="the tag to filter issues.")
    parser.add_argument("repo", help="The repository name")
    args = parser.parse_args()

    try:
        generate_msg_from_repo(args.repo, args.tag)
    except AssertionError:
        print(args.tag)
