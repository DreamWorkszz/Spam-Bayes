package bayes.spam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xpath.internal.compiler.Keywords;

public class BayesSpam {

	public static void main(String[] args) {
		BayesSpam bayes = new BayesSpam();
		List<List<String>> trainingMailList = bayes.getWords("ml-trainingset/SMSSpamCollection.txt");
		double spam_prior = 0.0;
		double ham_prior = 0.0;
		int spam_mail_count=0, ham_mail_count=0;
		
		HashMap<String, KeyWord> vocab = new HashMap<>();
		for (List<String> s : trainingMailList) {
			//先验概率,p(class=1)和p(class=0)
			if (s.get(0).equals("spam")) {
				spam_mail_count++;
			} else {
				ham_mail_count++;
			}
			//词汇表
			for (int i=1; i<s.size(); i++) {
				String str = s.get(i);
				vocab.put(str, new KeyWord(str,0,0,1.0,1.0));
			}
		}
		spam_prior = (double)spam_mail_count/(double)(spam_mail_count+ham_mail_count);
		ham_prior = 1-spam_prior;
		spam_prior = Math.log(spam_prior);
		ham_prior = Math.log(ham_prior);

		
		//计算词汇表中每个单词的条件概率，写入到KeyWord对象中
		int spam_total_count=0;
		int ham_total_count=0;
		for (List<String> mail : trainingMailList) {
			if (mail.get(0).equals("spam")) {
				for (Map.Entry<String, KeyWord> entry : vocab.entrySet()) {
					if (bayes.containKeyWord(mail.toString(), entry.getKey().toString())) {
						entry.getValue().key_spam++;
						spam_total_count++;
					}
				}
			} else {
				for (Map.Entry<String, KeyWord> entry : vocab.entrySet()) {
					if (bayes.containKeyWord(mail.toString(), entry.getKey().toString())) {
						entry.getValue().key_ham++;
						ham_total_count++;
					}
				}
			}
		}
		for (Map.Entry<String, KeyWord> entry : vocab.entrySet()) {
			KeyWord kw = entry.getValue();
			if (kw.key_spam!=0)
				kw.spam_prob = (double)kw.key_spam/(double)spam_total_count;
			if (kw.key_ham!=0)
				kw.ham_prob = (double)kw.key_ham/(double)ham_total_count;
			kw.spam_prob = Math.log(kw.spam_prob);
			kw.ham_prob = Math.log(kw.ham_prob);
		}
		//读入测试数据,计算后验概率
		double spam_posterior = spam_prior;
		double ham_posterior = ham_prior;
		int spamTotal=0, spamRight=0;
		
		List<List<String>> testing = bayes.getWords("ml-testset/TestFile.txt");
		for (List<String> mail : testing) {
			for (Map.Entry<String, KeyWord> entry : vocab.entrySet()) {
				if (bayes.containKeyWord(mail.toString(), entry.getKey().toString())) {
					spam_posterior += entry.getValue().spam_prob;
					ham_posterior += entry.getValue().ham_prob;
				}
			}
			if (mail.get(0).equals("spam")) {
				if (spam_posterior>=ham_posterior) {
					spamRight++;
				} 
				spamTotal++;
			}
			
			spam_posterior = spam_prior;
			ham_posterior = ham_prior;
		}
		System.out.println("Total spam: "+spamTotal);
		System.out.println("Right recognized: "+spamRight);
	}

	private List<List<String>> getWords(String filename) {
		StringBuffer sb = new StringBuffer();
		List<String> line = new ArrayList<>();
		List<List<String>> result = new ArrayList<>();
		BufferedReader br = null;
		File file = null;
		try {
			file = new File(filename);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str.trim());
				String[] a = sb.toString().split("[^a-zA-Z]+");
				for (int i=0; i<a.length; i++)
					line.add(a[i]);
				result.add(line);
				line = new ArrayList<>();
				sb = new StringBuffer();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 将banword 的关键字词与邮件内容逐字比较，若邮件内容中包含此关键字，则返回true
	 * 
	 * @param strContent
	 * @param strKeyWord
	 * @return
	 */
	private boolean containKeyWord(String strContent, String strKeyWord)
	{
		boolean retVal = false;

		if (strContent.toLowerCase().indexOf(strKeyWord.toLowerCase()) >= 0)
		{
			retVal = true;
		}

		return retVal;
	}
	
}

class KeyWord {
	String key;
	int key_spam;
	int key_ham;
	double spam_prob;
	double ham_prob;
	
	KeyWord(String key, int spam, int ham, double prob1, double prob2) {
		this.key = key;
		this.key_ham = ham;
		this.key_spam = spam;
		this.spam_prob = prob1;
		this.ham_prob = prob2;
	}
}






























