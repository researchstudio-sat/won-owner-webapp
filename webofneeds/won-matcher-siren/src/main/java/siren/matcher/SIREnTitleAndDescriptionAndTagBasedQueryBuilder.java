package siren.matcher;

import com.hp.hpl.jena.vocabulary.DC;
import com.sindicetech.siren.qparser.tree.dsl.ConciseQueryBuilder;
import com.sindicetech.siren.qparser.tree.dsl.ConciseTwigQuery;
import com.sindicetech.siren.qparser.tree.dsl.TwigQuery;
import config.SirenMatcherConfig;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import won.protocol.vocabulary.WON;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is an implementaion of SIREnQueryBuilderInterface that only uses a "need title" for making a SIREn matching
 *
 * @author soheilk
 * @date on 11.08.2015.
 */
@Component
public class SIREnTitleAndDescriptionAndTagBasedQueryBuilder implements SIREnQueryBuilderInterface {

    @Autowired
    private SirenMatcherConfig config;

    public String sIRENQueryBuilder(NeedObject needObject) throws QueryNodeException, IOException {


        //Since use the concise model to be able to also query over attributes
        ConciseQueryBuilder build = new ConciseQueryBuilder();
        ConciseTwigQuery topTwig = build.newTwig("@graph");

        TwigQuery twigBasicNeedType = null;
        //First of all, we have to cinsider the BasicNeedType
        switch (needObject.getBasicNeedType().toLowerCase()) { //Attention: lower-case
            //TODO: replace the strings with WON. constants already there
            case "http://purl.org/webofneeds/model#supply": // Demands has to be matched
                twigBasicNeedType = build.newTwig("http://purl.org/webofneeds/model#hasBasicNeedType")
                        .with(build.newNode("'http://purl.org/webofneeds/model#demand'").setAttribute("@id"));
                break;
            case "http://purl.org/webofneeds/model#demand":
                twigBasicNeedType = build.newTwig("http://purl.org/webofneeds/model#hasBasicNeedType")
                        .with(build.newNode("'http://purl.org/webofneeds/model#supply'").setAttribute("@id"));
                break;
            case "http://purl.org/webofneeds/model#dotogether":
                twigBasicNeedType = build.newTwig("http://purl.org/webofneeds/model#hasBasicNeedType")
                        .with(build.newNode("'http://purl.org/webofneeds/model#dotogether'").setAttribute("@id"));
                break;
        }

        // processing the title and make some queries out of it

        QueryNLPProcessor qNLPP = new QueryNLPProcessor();


        String[] tokenizedTitlePhrase = qNLPP.extractRelevantWordTokens(needObject.getNeedTitle());

        ArrayList<TwigQuery> twigTitleArrayList = new ArrayList<TwigQuery>();

        for (int i = 0; i < tokenizedTitlePhrase.length && i < config.getConsideredQueryTokens() / 2; i++) {
            twigTitleArrayList.add(build.newTwig(WON.HAS_CONTENT.toString())
                    .with(build.newNode(tokenizedTitlePhrase[i]).setAttribute(DC.title.toString())));
        }

        String[] tokenizedDescriptionPhrase = qNLPP.extractRelevantWordTokens(needObject.getNeedDescription());

        ArrayList<TwigQuery> twigDescriptionArrayList = new ArrayList<TwigQuery>();

        for (int i = 0; i < tokenizedDescriptionPhrase.length && i < config.getConsideredQueryTokens() / 2; i++) {
            twigDescriptionArrayList.add(build.newTwig(WON.HAS_CONTENT.toString())
                    .with(build.newNode(tokenizedDescriptionPhrase[i]).setAttribute(WON.HAS_TEXT_DESCRIPTION.toString())));
        }


        String[] tokenizedTagPhrase = qNLPP.extractRelevantWordTokens(needObject.getNeedTag());

        ArrayList<TwigQuery> twigTagArrayList = new ArrayList<TwigQuery>();

        for (int i = 0; i < tokenizedTagPhrase.length && i < config.getConsideredQueryTokens() / 2; i++) {
            twigTagArrayList.add(build.newTwig(WON.HAS_CONTENT.toString())
                    .with(build.newNode(tokenizedTagPhrase[i]).setAttribute(WON.HAS_TAG.toString())));
        }


        if (twigBasicNeedType != null)
            topTwig.with(twigBasicNeedType);
        for (int j = 0; j < twigTitleArrayList.size(); j++) {
            topTwig.optional(twigTitleArrayList.get(j));
        }
        for (int k = 0; k < twigDescriptionArrayList.size(); k++) {
            topTwig.optional(twigDescriptionArrayList.get(k));
        }
        for (int l = 0; l < twigTagArrayList.size(); l++) {
            topTwig.optional(twigTagArrayList.get(l));
        }

        return topTwig.toString();

    }

}
