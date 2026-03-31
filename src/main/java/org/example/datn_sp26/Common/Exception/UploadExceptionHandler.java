package org.example.datn_sp26.Common.Exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Ảnh upload quá dung lượng cho phép. Vui lòng chọn ảnh nhỏ hơn (<= 20MB).");
        return "redirect:/admin/san-pham";
    }
}

