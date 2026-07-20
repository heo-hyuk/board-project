package com.board.controller;

import com.board.service.DataDictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 데이터사전(테이블/인덱스/뷰/FK) 조회 + DDL(DROP/CREATE) 동작 확인용 관리 화면
// 로그인한 사용자만 접근 가능 (SecurityConfig의 anyRequest().authenticated() 기본 정책 적용)
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final DataDictionaryService dataDictionaryService;

    @GetMapping("/admin/datadic")
    public String dataDictionary(Model model) {
        model.addAttribute("dic", dataDictionaryService.getDataDictionary());
        return "admin/datadic";
    }

    // post_stats 테이블에 대해 DROP → CREATE를 실제로 실행하여 DDL 동작을 눈으로 확인
    @PostMapping("/admin/datadic/ddl-demo")
    public String runDdlDemo(RedirectAttributes redirectAttributes) {
        dataDictionaryService.redoPostStatsTable();
        redirectAttributes.addFlashAttribute("successMsg",
                "post_stats 테이블에 DROP TABLE → CREATE TABLE을 실행했습니다.");
        return "redirect:/admin/datadic";
    }
}
