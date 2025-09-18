# Jira Commit Assistant IntelliJ Plugin

이 저장소는 커밋 메시지 앞에 자동으로 Jira 티켓 키를 붙여주는 IntelliJ IDEA 플러그인을 제공합니다. 커밋 시점에 최근 Jira 이슈 목록이 표시되고, 원하는 이슈를 선택하면 `[KEY] message` 형식으로 메시지가 채워집니다.

## 주요 기능
- **자동 프리픽스**: 커밋 버튼을 눌렀을 때 Jira 이슈 선택 창이 열리고, 선택한 티켓 키가 메시지 앞에 자동으로 붙습니다.
- **JQL 기반 목록**: 설정한 JQL을 이용해 최대 10개의 최근 이슈를 불러옵니다.
- **검색 지원**: 선택 창의 검색 필드를 통해 다른 티켓을 즉시 찾을 수 있습니다.
- **API 변경 대응**: Jira REST API v3 실패 시 v2로 자동 폴백하여 최신 변경 사항에 유연하게 대응합니다.

## 설정 방법
1. `File | Settings | Tools | Jira Commit Assistant` 메뉴를 열어 다음 정보를 입력합니다.
   - Jira Base URL (예: `https://your-domain.atlassian.net`)
   - User Email (Jira Cloud API 토큰을 사용하는 경우 이메일)
   - API Token
   - 기본 JQL (선택 사항, 비워두면 최신 업데이트 순으로 정렬)
2. 설정을 저장하면 다음 커밋부터 선택 창이 활성화됩니다.

## 빌드 및 설치
모든 의존성은 Gradle이 자동으로 내려받습니다. 다음 명령 한 줄로 플러그인 패키지를 생성할 수 있습니다.

```bash
./gradlew buildPlugin
```

빌드가 완료되면 `build/distributions/` 디렉터리에 `.zip` 파일이 생성됩니다. IntelliJ IDEA에서 `Settings | Plugins | ⚙ | Install Plugin from Disk...` 메뉴로 해당 ZIP을 선택해 설치할 수 있습니다.

만약 Gradle Wrapper를 사용할 수 없는 환경이라면 `gradle buildPlugin` 명령을 실행해도 동일하게 동작합니다.

## 배포 스크립트 (선택 사항)
간단한 쉘 스크립트 `build_plugin.sh`를 제공하니 필요에 따라 사용하세요.

```bash
./build_plugin.sh
```

해당 스크립트는 기존 산출물을 정리한 후 플러그인 ZIP 파일을 생성합니다.
