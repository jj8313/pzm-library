// 관리자 톡톡 (admin/talkChat.html)
document.addEventListener("DOMContentLoaded", function () {

  const chat = document.getElementById("chat");
  const input = document.getElementById("content");
  const form = document.getElementById("talkForm");
  const memberId = form.dataset.memberId;        // 지금 보고 있는 회원 번호 (HTML에서 꺼냄)
  const memberName = chat.dataset.memberName;    // 회원 이름표: html의 data-member-name (언어 따라 바뀜)

  // 지금 시각을 화면 언어로: 오후 3:20 / 午後3:20
  function nowTime() {
    const lang = document.documentElement.lang || "ko";
    return new Date().toLocaleTimeString(lang, { hour: "numeric", minute: "2-digit", hour12: true });
  }

  chat.scrollTop = chat.scrollHeight;

  // ① 서버에 연결 (https면 wss, http면 ws)
  const protocol = (location.protocol === "https:") ? "wss://" : "ws://";
  const ws = new WebSocket(protocol + location.host + "/ws/talk");

  // ② 보내기: 회원 번호도 같이
  form.addEventListener("submit", function (e) {
    e.preventDefault();

    const content = input.value.trim();
    if (content === "") return;

    ws.send(JSON.stringify({ memberId: memberId, content: content }));
    input.value = "";
  });

  // ③ 받으면 → 말풍선 붙이기
  ws.onmessage = function (event) {
    const m = JSON.parse(event.data);

    // 다른 회원 방 메시지면 무시
    if (String(m.memberId) !== memberId) return;

    const row = document.createElement("div");

    if (m.fromAdmin) {
      // 도서관(나) → 오른쪽
      row.className = "row me";
      row.innerHTML = '<span class="time"></span><div class="bubble"></div>';
    } else {
      // 회원 → 왼쪽
      row.className = "row other";
      row.innerHTML = '<div><div class="name"></div><div class="bubble"></div></div>'
                    + '<span class="time"></span>';
      row.querySelector(".name").textContent = memberName;
    }

    row.querySelector(".bubble").textContent = m.content;
    row.querySelector(".time").textContent = nowTime();   // 서버 시간 대신 화면 언어로 만든 지금 시각

    chat.appendChild(row);
    chat.scrollTop = chat.scrollHeight;
  };

});