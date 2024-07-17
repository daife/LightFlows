package com.dypho.roughdict;

import org.json.JSONObject;
import org.json.JSONArray;

public class Utils {
    public static String jsonProcess(String jsonInput) {
        // String jsonInput = "{...}"; // Your JSON input goes here
        JSONObject jsonObj = null;
        String usPhone = "";
        String ukPhone = "";
        String meanings = "";
        String wordForms = "";
        String relatedWords = "";
        String phrasesAndMeanings = "";
        String exampleSentences = "";
        String translationInput = "";
        String translation = "";
        String synonyms = "";
        String result = "";
        String wordSummary ="";
        try {
            jsonObj = new JSONObject(jsonInput);

            // Extracting pronunciation and meanings (ec section)
            JSONObject ec = jsonObj.getJSONObject("ec");
            JSONObject word = ec.getJSONArray("word").getJSONObject(0);
            // System.out.println(word);
            try {
                usPhone = word.getString("usphone");
                ukPhone = word.getString("ukphone");
            } catch (Exception err) {
            }
            JSONArray trs = word.getJSONArray("trs");
            // System.out.println(trs);
            StringBuilder meaningsBuilder = new StringBuilder();
            for (int i = 0; i < trs.length(); i++) {
                JSONArray trArray = trs.getJSONObject(i).getJSONArray("tr");
                // System.out.println(trArray);
                for (int j = 0; j < trArray.length(); j++) {
                    meaningsBuilder.append(
                            trArray.getJSONObject(j)
                                    .getJSONObject("l")
                                    .getJSONArray("i")
                                    .join(", ")
                                    .replaceAll("\"", ""));
                    if (i < trs.length() - 1) {
                        meaningsBuilder.append(",\n ");
                    }
                }
            }
            meanings = meaningsBuilder.toString();
            System.out.println(meanings);

            // Extracting word forms (ec section)
            JSONArray wfs = word.getJSONArray("wfs");
            StringBuilder wordFormsBuilder = new StringBuilder();
            for (int i = 0; i < wfs.length(); i++) {
                JSONObject wf = wfs.getJSONObject(i).getJSONObject("wf");
                wordFormsBuilder
                        .append(wf.getString("name"))
                        .append("：")
                        .append(wf.getString("value"));
                if (i < wfs.length() - 1) {
                    wordFormsBuilder.append(",\n ");
                }
            }
            wordForms = wordFormsBuilder.toString();
        } catch (Exception err) {

        }

        try { // Extracting related words (rel_word section)
            JSONObject relWord = jsonObj.getJSONObject("rel_word");
            JSONArray rels = relWord.getJSONArray("rels");
            StringBuilder relatedWordsBuilder = new StringBuilder();
            for (int i = 0; i < rels.length(); i++) {
                JSONObject rel = rels.getJSONObject(i).getJSONObject("rel");
                JSONArray words = rel.getJSONArray("words");
                for (int j = 0; j < words.length(); j++) {
                    JSONObject wordObj = words.getJSONObject(j);
                    relatedWordsBuilder
                            .append(wordObj.getString("word"))
                            .append(" - ")
                            .append(wordObj.getString("tran").trim());
                    if (j < words.length() - 1) {
                        relatedWordsBuilder.append(",\n ");
                    }
                }
                if (i < rels.length() - 1) {
                    relatedWordsBuilder.append(";\n");
                }
            }
            relatedWords = relatedWordsBuilder.toString();
        } catch (Exception err) {

        }
        try {
            // Extracting phrases and their meanings (phrs section)
            JSONObject phrs = jsonObj.getJSONObject("phrs");
            JSONArray phrases = phrs.getJSONArray("phrs");
            StringBuilder phrasesBuilder = new StringBuilder();
            int phrasesCount = Math.min(phrases.length(), 5); // Limit to the first five phrases
            for (int i = 0; i < phrasesCount; i++) {
                JSONObject phraseObj = phrases.getJSONObject(i).getJSONObject("phr");
                String phrase =
                        phraseObj.getJSONObject("headword").getJSONObject("l").getString("i");
                JSONArray trsArray = phraseObj.getJSONArray("trs");
                for (int j = 0; j < trsArray.length(); j++) {
                    String meaning =
                            trsArray.getJSONObject(j)
                                    .getJSONObject("tr")
                                    .getJSONObject("l")
                                    .getString("i");
                    phrasesBuilder.append(phrase).append(" - ").append(meaning);
                    if (j < trsArray.length() - 1) {
                        phrasesBuilder.append(",\n ");
                    }
                }
                if (i < phrasesCount - 1) {
                    phrasesBuilder.append(",\n ");
                }
            }
            phrasesAndMeanings = phrasesBuilder.toString();
        } catch (Exception err) {

        }
        try {
            // Extracting two bilingual example sentences (blng_sents_part section)
            JSONObject blngSentsPart = jsonObj.getJSONObject("blng_sents_part");
            JSONArray sentencePairs = blngSentsPart.getJSONArray("sentence-pair");
            StringBuilder exampleSentencesBuilder = new StringBuilder();
            int sentenceCount =
                    Math.min(sentencePairs.length(), 2); // Limit to the first two sentences
            for (int i = 0; i < sentenceCount; i++) {
                JSONObject sentencePair = sentencePairs.getJSONObject(i);
                String sentenceEng =
                        sentencePair
                                .getString("sentence-eng")
                                .replace("<b>", "☛")
                                .replace("</b>", "☚");
                String sentenceTrans = sentencePair.getString("sentence-translation");
                exampleSentencesBuilder.append(sentenceEng).append(" - ").append(sentenceTrans);
                if (i < sentenceCount - 1) {
                    exampleSentencesBuilder.append(",\n ");
                }
            }
            exampleSentences = exampleSentencesBuilder.toString();
        } catch (Exception err) {

        }
        try {
            // Extracting translation (fanyi section)
            JSONObject fanyi = jsonObj.getJSONObject("fanyi");
            translationInput = fanyi.getString("input");
            translation = fanyi.getString("tran");

        } catch (Exception err) {

        }
        try { // Extracting synonyms (ce section)
            // Extracting synonyms (ce section)
            JSONObject ce = jsonObj.getJSONObject("ce");
            JSONArray wordsArray = ce.getJSONArray("word");
            StringBuilder synonymsBuilder = new StringBuilder();
            for (int i = 0; i < wordsArray.length(); i++) {
                JSONArray trsArray = wordsArray.getJSONObject(i).getJSONArray("trs");
                for (int j = 0; j < trsArray.length(); j++) {
                    JSONObject trObj =
                            trsArray.getJSONObject(j).getJSONArray("tr").getJSONObject(0);
                    JSONArray itemsArray = trObj.getJSONObject("l").getJSONArray("i");
                    StringBuilder phraseBuilder = new StringBuilder();
                    for (int k = 0; k < itemsArray.length(); k++) {
                        Object item = itemsArray.get(k);
                        if (item instanceof JSONObject) {
                            JSONObject itemObj = (JSONObject) item;
                            if (itemObj.has("#text")) {
                                phraseBuilder.append(itemObj.getString("#text")).append(" ");
                            }
                        } else if (item instanceof String) {
                            phraseBuilder.append((String) item);
                        }
                    }
                    String phrase = phraseBuilder.toString().trim();
                    synonymsBuilder.append(phrase);
                    if (j < trsArray.length() - 1) {
                        synonymsBuilder.append(";\n");
                    }
                }
            }
            synonyms = synonymsBuilder.toString();

        } catch (Exception err) {

        }

        try {
            // Extracting translation (fanyi section)
            JSONObject fanyi = jsonObj.getJSONObject("fanyi");
            translationInput = fanyi.getString("input");
            translation = fanyi.getString("tran");
        } catch (Exception err) {

        }
        try { // Extracting word summary (baike section)
JSONObject baike = jsonObj.getJSONObject("baike");
JSONArray summarysArray = baike.getJSONArray("summarys");
//String wordSummary = "";
for (int i = 0; i < summarysArray.length(); i++) {
    JSONObject summaryObj = summarysArray.getJSONObject(i);
    String summary = summaryObj.getString("summary");
    wordSummary = summary; // Assuming there's only one summary, you can modify this if needed
}

        } catch (Exception err) {

        }
        try {
            // Constructing the result string with all sections including synonyms
            result =
                    "US ["
                            + usPhone
                            + "], UK ["
                            + ukPhone
                            + "]\n\n"
                            + "●释义: "
                            + meanings
                            + "\n\n"
                            + synonyms
            +"\n"
                            + "●变形:"
                            + wordForms
                            + "\n\n"
                            + "●同根: "
                            + relatedWords
                            + "\n\n"
                            + "●搭配: "
                            + phrasesAndMeanings
                            + "\n\n"
                            + "●例句: \n"
                            + exampleSentences
                            + "\n\n"
                            + "●机翻: "
                            + translationInput
                            + " - "
                            + translation
             + "\n\n"
                            + "●百科: "
                            + wordSummary;

            // System.out.println(result);
        } catch (Exception err) {

        }
        return result;
    }
}
