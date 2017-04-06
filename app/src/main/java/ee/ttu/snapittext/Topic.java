package ee.ttu.snapittext;


import java.util.Date;

public class Topic {

    public String topicName;
    public String topicCreator;
    public long topicTime;

    public Topic(String topicName, String topicCreator) {
        this.topicName = topicName;
        this.topicCreator = topicCreator;

        this.topicTime = new Date().getTime();

    }

    public Topic() {

    }

    public String getTopicCreator() {
        return topicCreator;
    }

    public void setTopicCreator(String topicCreator) {
        this.topicCreator = topicCreator;
    }

    public long getTopicTime() {
        return topicTime;
    }

    public void setTopicTime(long topicTime) {
        this.topicTime = topicTime;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
