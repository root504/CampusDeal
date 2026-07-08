package com.campusdeal.dto.request;

import jakarta.validation.constraints.NotBlank;

public class MessageRequest {

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long toUserId;

    private Long productId;

    private String messageType;

    public MessageRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
    public Long getReceiverId() { return toUserId; }
    public void setReceiverId(Long receiverId) { this.toUserId = receiverId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}
