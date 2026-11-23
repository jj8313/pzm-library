// 회원 톡톡 (talk.html)
document.addEventListener("DOMContentLoaded", function () {

  const chat = document.getElementById("chat");
  const input = document.getElementById("content");
  const form = document.getElementById("talkForm");

  // 도서관 이름: html의 data-admin-name에서 꺼냄 (언어 따라 바뀜)
  const adminName = chat.dataset.adminName;

  // 지금 시각을 화면 언어로: 오후 3:20 / 午後3:20
  function nowTime() {
    const lang = document.documentElement.lang || "ko";
    return new Date().toLocaleTimeString(lang, { hour: "numeric", minute: "2-digit", hour12: true });
  }

  // 처음에 맨 아래로
  chat.scrollTop = chat.scrollHeight;

  // ① 서버에 연결 (https면 wss, http면 ws)
  const protocol = (location.protocol === "https:") ? "wss://" : "ws://";
  const ws = new WebSocket(protocol + location.host + "/ws/talk");

  // ② 보내기
  form.addEventListener("submit", function (e) {
    e.preventDefault();                    // 새로고침 막기

    const content = input.value.trim();
    if (content === "") return;

    ws.send(JSON.stringify({ content: content }));
    input.value = "";
  });

  // ③ 받으면 → 말풍선 붙이기
  ws.onmessage = function (event) {
    const m = JSON.parse(event.data);

    const row = document.createElement("div");

    if (m.fromAdmin) {
      // 도서관 → 왼쪽
      row.className = "row admin";
      row.innerHTML = '<div><div class="name"></div><div class="bubble"></div></div>'
                    + '<span class="time"></span>';
      row.querySelector(".name").textContent = adminName;
    } else {
      // 나 → 오른쪽
      row.className = "row me";
      row.innerHTML = '<span class="time"></span><div class="bubble"></div>';
    }

    row.querySelector(".bubble").textContent = m.content;
    row.querySelector(".time").textContent = nowTime();   // 서버 시간 대신 화면 언어로 만든 지금 시각

    chat.appendChild(row);
    chat.scrollTop = chat.scrollHeight;
  };

});