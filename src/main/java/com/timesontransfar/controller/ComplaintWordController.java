package com.timesontransfar.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
public class ComplaintWordController {
	protected Logger log = LoggerFactory.getLogger(ComplaintWordController.class);

	@PostMapping("/workflow/word/generateWord")
	public ResponseEntity<byte[]> generateWord(@RequestBody String parm) {
		log.info("generateWord parm: {}", parm);
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = (String) json.get("orderId");
		String miitCode = (String) json.get("miitCode");
		String custName = (String) json.get("custName");
		String appealReason = (String) json.get("appealReason");
		String userInfo = (String) json.get("userInfo");
		String handlingSituation = (String) json.get("handlingSituation");
		String processResult = (String) json.get("processResult");
		String title = "关于江苏省客户" + custName + "申诉处理情况的核查报告";//文件名
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (XWPFDocument document = new XWPFDocument()) {
			addParagraph(document, title, ParagraphAlignment.CENTER, 16, true, false);
			addParagraph(document, "（" + miitCode + "）", ParagraphAlignment.CENTER, 16, true, false);
			addParagraph(document, "江苏省管局申诉受理中心：", ParagraphAlignment.LEFT, 15, true, false);
			String para = "贵中心转来我省" + custName + "客户申诉问题已收悉，我公司高度重视，立即责成分公司展开核查，现将核查处理情况汇报如下：";
			addParagraph(document, para, ParagraphAlignment.LEFT, 15, false, true);
			addParagraph(document, "一、客户申诉事由", ParagraphAlignment.LEFT, 15, true, false);
			splitAndAdd(document, appealReason);
			addParagraph(document, "二、申诉客户基本信息", ParagraphAlignment.LEFT, 15, true, false);
			splitAndAdd(document, userInfo);
			addParagraph(document, "三、核查处理情况", ParagraphAlignment.LEFT, 15, true, false);
			splitAndAdd(document, handlingSituation);
			addParagraph(document, "四、本次申诉处理结果", ParagraphAlignment.LEFT, 15, true, false);
			splitAndAdd(document, processResult);
			addParagraph(document, "五、整改举措", ParagraphAlignment.LEFT, 15, true, false);
			splitAndAdd(document, "无");
			document.write(baos);
		} catch (IOException e) {
			log.info("err：{},{},{}", orderId, "XWPFDocument", e.getMessage());
		}
		try {
			String fileName = title + ".docx";
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
			return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
		} catch (UnsupportedEncodingException e) {
			log.info("err：{},{},{}", orderId, "UnsupportedEncodingException", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private void splitAndAdd(XWPFDocument document, String para) {
		String[] paras = para.split("\\n");
		for (String text : paras) {
			text = text.replace("\\t", "");
			text = text.trim();
			addParagraph(document, text, ParagraphAlignment.LEFT, 15, false, true);
		}
	}

	private void addParagraph(XWPFDocument document, String text, ParagraphAlignment align, int fontSize, boolean bold, boolean indent) {
		XWPFParagraph paragraph = document.createParagraph();
		CTPPr pPr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
		CTInd ind = pPr.isSetInd() ? pPr.getInd() : pPr.addNewInd();
		if (!indent && ind.isSetFirstLine()) {
			ind.unsetFirstLine();
		} else if (indent) {
			ind.setFirstLine(BigInteger.valueOf(720));
		}
		paragraph.setAlignment(align);
		XWPFRun run = paragraph.createRun();
		run.setFontFamily("仿宋");
		run.setFontSize(fontSize);
		run.setBold(bold);
		run.setText(text);
	}
}