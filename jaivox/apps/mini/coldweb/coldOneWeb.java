/*
   Jaivox version 0.5 August 2013
   Copyright 2010-2013 by Bits and Pixels, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.Interact;
import com.jaivox.util.Log;
import com.jaivox.recognizer.web.SpeechInput;
import com.jaivox.recognizer.web.Mike;
import com.jaivox.synthesizer.web.Synthesizer;
import java.util.Properties;

public class coldOneWeb {

	static String basedir = "./";
	Interact inter;
	Synthesizer speaker;
	static int wait = 20; // maximum length of input in seconds
	static String type = ".wav";

	public coldOneWeb () {
		Log log = new Log ();
		log.setLevelByName ("warning");
		initializeInterpreter ();
		speaker = new Synthesizer ();
		processSpeech ();
	}

	void initializeInterpreter () {
		Properties kv = new Properties ();
// 		kv.setProperty ("data_file", "PATdata_file");
		kv.setProperty ("common_words", "common_en.txt");
// 		kv.setProperty ("specs_file", "PATspecs_file");
		kv.setProperty ("questions_file", "cold.quest");
		kv.setProperty ("grammar_file", "cold.dlg");
		Command cmd = new Command ();
		inter = new Interact (basedir, kv, cmd);
	}

	void processSpeech () {
		SpeechInput R = new SpeechInput ();
		Mike mike = new Mike ("cold", type);
		int empty = 0;
		int maxempty = 5;
		while (true) {
			String flac = mike.nextsample (type, wait);
			mike.showtime ("result is "+flac);
			if (flac != null) {
				String result = R.recognize (flac, "en-US");
				System.out.println ("Recognized: " + result);
				String response = "";
				if (result != null) {
					if (result.trim ().equals ("")) {
						empty++;
						if (empty >= maxempty) return;
					}
					response = inter.execute (result);
					System.out.println ("Reply: " + response);
				}
				try {
					Thread.sleep (4000);
					// can't always play flac
					speaker.speak (response);
				} catch (Exception e) {
					e.printStackTrace ();
					break;
				}
			}
		}
	}

	public static void main (String args []) {
		new coldOneWeb ();
	}
}

