package won.bot.framework.eventbot.action.impl.mail.receive.util;

import won.protocol.model.BasicNeedType;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailContentExtractor {
    //TODO: IMPLEMENT VOCABULARY VIA ADMIN INTERFACE OR PROPERTIES FILE THAT LINKS CERTAIN REGULAREXPRESSIONS WITH A BasicNeedType
    public static final String KEYWORD_TYPE_DEMAND = "[WANT]";
    public static final String KEYWORD_TYPE_SUPPLY = "[OFFER]";
    public static final String KEYWORD_TYPE_DO_TOGETHER = "[TOGETHER]";
    public static final String KEYWORD_TYPE_CRITIQUE = "[CRITIQUE]";
    public static final String TAGEXTRACTION_PATTERN_STRING = "[#]+[\\w]+\\b";

    public static final Pattern tagExtractionPattern = Pattern.compile(TAGEXTRACTION_PATTERN_STRING);

    public static String getTitle(MimeMessage message) throws MessagingException {
        String subject = message.getSubject();
        //TODO: implement this method way smarter than that;

        subject = subject
                    .replace(KEYWORD_TYPE_DEMAND, "")
                    .replace(KEYWORD_TYPE_SUPPLY, "")
                    .replace(KEYWORD_TYPE_DO_TOGETHER, "")
                    .replace(KEYWORD_TYPE_CRITIQUE, "")
                    .trim();
        return subject;
    }

    public static String getDescription(MimeMessage message) throws MessagingException, IOException {
        return message.getContent().toString();
    }

    public static String getTextMessage(MimeMessage message) throws MessagingException, IOException {
        return getTextMessage(message.getContent());
    }

    public static String getTextMessage(Object content) {
        //TODO: make message extraction a little bit more sophisticated
        return content.toString().split("\\r")[0];
    }

    public static BasicNeedType getBasicNeedType(MimeMessage message) throws MessagingException {
        return getBasicNeedType(message.getSubject());
    }

    public static BasicNeedType getBasicNeedType(String subject) {
        if(subject.startsWith(KEYWORD_TYPE_DEMAND)){
            return BasicNeedType.DEMAND;
        }else if(subject.startsWith(KEYWORD_TYPE_SUPPLY)){
            return BasicNeedType.SUPPLY;
        }else if(subject.startsWith(KEYWORD_TYPE_DO_TOGETHER)){
            return BasicNeedType.DO_TOGETHER;
        }else if(subject.startsWith(KEYWORD_TYPE_CRITIQUE)){
            return BasicNeedType.CRITIQUE;
        }else{
            return null;
        }
    }

    public static String[] getTags(MimeMessage message) throws MessagingException, IOException {
        HashSet<String> tags = new HashSet<>();

        Matcher m = tagExtractionPattern.matcher(new StringBuilder(message.getSubject()).append(" ").append(message.getContent()).toString());

        while(m.find()) {
            tags.add(m.group());
        }

        String[] tagArray = new String[tags.size()];
        tagArray = tags.toArray(tagArray);
        Arrays.sort(tagArray);

        return tagArray;
    }

    public static String getFromAddressString(MimeMessage message) throws MessagingException{
        Address[] froms = message.getFrom();
        return (froms == null) ? null : ((InternetAddress) froms[0]).getAddress();
    }
}