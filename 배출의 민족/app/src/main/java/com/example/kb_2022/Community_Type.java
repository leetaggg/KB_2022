package com.example.kb_2022;

public class Community_Type {

    private String Writer;
    private String Title;
    private String Like;
    private String Number;
    private String Content;

    public String getWriter() {
        return Writer;
    }
    public String getTitle() {
        return Title;
    }
    public String getLike(){
        return Like;
    }
    public String getNumber(){
        return Number;
    }
    public String getContent(){
        return Content;
    }
    public void setContent(String content ) { this.Content = content; }
    public void setWriter(String writer) {
        this.Writer = writer;
    }
    public void setTitle(String title) {
        this.Title = title;
    }
    public void setLike(int like) { this.Like = Integer.toString(like); }
    public void setNumber(int number) { this.Number = Integer.toString(number); }
}
