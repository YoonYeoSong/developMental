package com.kh.workman.collabo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kh.workman.collabo.model.service.CollaboService;
import com.kh.workman.collabo.model.vo.DataPacket;

public class CollaboHandler extends TextWebSocketHandler {
	private static Map<String, WebSocketSession> sessionList = new HashMap<String, WebSocketSession>();
	private static Map<String, Integer> collaboList = new HashMap<String, Integer>();
	private Logger logger = LoggerFactory.getLogger(CollaboHandler.class);

	@Autowired
	CollaboService service;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessionList.remove(session);
		logger.debug("{} 연결종료", session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		DataPacket receive = parsingJson(message.getPayload());
		System.out.println(receive);
//		ObjectMapper om = new ObjectMapper();
//		Map<String, Object> temp = om.readValue(message.getPayload(), Map.class);
//		System.out.println(temp);
		switch (receive.getType()) {
		case "connect":
			sessionList.put(receive.getUserId(), session);
			collaboList.put(receive.getUserId(), receive.getCollaboNo());
			logger.debug(receive.getUserId() + " 님이 " + collaboList.get(receive.getUserId())
					+ "번 방에 접속하셨습니다. SESSION ID = {}", sessionList.get(receive.getUserId()).getId());
			break;
		case "list":
			switch (receive.getMethod()) {
			case "create":
				createList(receive, session);
				break;
			case "update":
				break;
			case "delete":
				break;
			}
			break;
		case "card":
			switch (receive.getMethod()) {
			case "create":
				createCard(receive, session);
				break;
			case "update":
				updateCard(receive, session);
				break;
			case "delete":
				break;
			case "move":
				moveCard(receive, session);
				break;
			}
			break;
		}
	}

	private void updateCard(DataPacket receive, WebSocketSession session) throws IOException {
		boolean isCompleted = service.updateCard(receive) == 1 ? true : false;
		List<HashMap> collabos = service.participation(receive.getCollaboNo());

		if (isCompleted) {
			for (String key : sessionList.keySet()) {
				for (int i = 0; i < collabos.size(); i++) {
					if (key.equals(collabos.get(i).get("ID"))) {
						sessionList.get(key).sendMessage(new TextMessage(toJson(receive)));
						break;
					}
				}
			}
		}
		logger.debug("Move Card Success [USER ID : " + receive.getUserId() + " Card NO : " + receive.getCardNo() + "]");
	}
		

	private void createCard(DataPacket receive, WebSocketSession session) throws IOException {
		boolean isCompleted = service.createCard(receive) == 1 ? true : false;
		List<HashMap> collabos = service.participation(receive.getCollaboNo());

		if (isCompleted) {
			for (String key : sessionList.keySet()) {
				for (int i = 0; i < collabos.size(); i++) {
					if (key.equals(collabos.get(i).get("ID"))) {
						sessionList.get(key).sendMessage(new TextMessage(toJson(receive)));
						break;
					}
				}
			}
		}
		logger.debug(
				"Create Card Success [USER ID : " + receive.getUserId() + " CARD NO : " + receive.getCardNo() + "]");
	}

	public void createList(DataPacket receive, WebSocketSession session) throws IOException {
		boolean isCompleted = service.createList(receive) == 1 ? true : false;
		List<HashMap> collabos = service.participation(receive.getCollaboNo());

		if (isCompleted) {
			for (String key : sessionList.keySet()) {
				for (int i = 0; i < collabos.size(); i++) {
					if (key.equals(collabos.get(i).get("ID"))) {
						sessionList.get(key).sendMessage(new TextMessage(toJson(receive)));
						break;
					}
				}
			}
		}
		logger.debug(
				"Create List Success [USER ID : " + receive.getUserId() + " LIST NO : " + receive.getListNo() + "]");
	}

	public void moveCard(DataPacket receive, WebSocketSession session) throws IOException {
		boolean isCompleted = service.moveCard(receive) == 1 ? true : false;
		List<HashMap> collabos = service.participation(receive.getCollaboNo());

		if (isCompleted) {
			for (String key : sessionList.keySet()) {
				for (int i = 0; i < collabos.size(); i++) {
					if (key.equals(collabos.get(i).get("ID"))) {
						sessionList.get(key).sendMessage(new TextMessage(toJson(receive)));
						break;
					}
				}
			}
		}
		logger.debug("Move Card Success [USER ID : " + receive.getUserId() + " Card NO : " + receive.getCardNo() + "]");
	}

	public DataPacket parsingJson(String receiveMessage) {
		Gson gson = new GsonBuilder().create();
		DataPacket temp = gson.fromJson(receiveMessage, DataPacket.class);
		return temp;
	}

	public String toJson(Object obj) {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(obj);
	}

}
