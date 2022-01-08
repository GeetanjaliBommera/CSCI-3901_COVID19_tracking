import java.io.File;
import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 * Java code for MobileDevice
 * 
 * @author Geetanjali Bommera B00881511 MobileDevice Class helps to gather the
 *         contacts person contacted to and send this data to government
 *         database and finds whether contacted to any positive person
 */
public class MobileDevice {
	/**
	 * Global variables to use in different methods
	 */
	private ArrayList<String> testhash = new ArrayList<String>();
	private String networkaddress, networkname;
	private ArrayList<ArrayList<String>> contacts = new ArrayList<ArrayList<String>>();
	private String user;
	private String userhash, xmldata;
	private Government gov;
	private boolean positivetestflag = false;
	private int maxdate;
	/**
	 * MobileDevice method is the constructor hashes its own deviceid.
	 * 
	 * @param configFile    - File path for Device network name and network address
	 * @param contactTracer - Government object to send data
	 */
	public MobileDevice(String configFile, Government contactTracer) {
		try {
			if (configFile == null || configFile == "") {
				System.exit(0);
			}

			File myObj = new File(configFile);
			Scanner myReader = new Scanner(myObj);
			int flag = 0;
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				if (flag == 0) {
					String dname[] = data.split("=");
					networkaddress = dname[1];
					flag = 1;

				} else if (flag == 1) {
					String dname[] = data.split("=");
					networkname = dname[1];
					flag = 0;
				}

			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error in config file - Mobile Device");
			e.printStackTrace();
		}
		// hashing
		if (networkaddress == "" || networkaddress == null || networkname == "" || networkname == null) {
			System.exit(0);
		}
		user = networkaddress + networkname;
		userhash = toHash(user);
		gov = contactTracer;
		LocalDate localDate = LocalDate.now();//For reference
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
		String formattedString = localDate.format(formatter);
		maxdate = dateTodays(formattedString);
	}

	/**
	 * recordContact Method is used to record the contacts
	 * 
	 * @param individual - Contact id
	 * @param date       - date of contact
	 * @param duration   - duration of the contact
	 */
	public void recordContact(String individual, int date, int duration) {
		if (!(individual == null || individual == "")) {
			if (duration <= 1440 && duration > 0) {
				if(0<=date && date<=maxdate) {
				ArrayList<String> temp = new ArrayList<String>();
				String hashindividual = toHash(individual);
				temp.add(hashindividual);
				String d = String.valueOf(date);
				temp.add(d);
				String dur = String.valueOf(duration);
				temp.add(dur);
				contacts.add(temp);
				}
			}
		}

	}

	/**
	 * postiveTest method helps to send government and its contacts to know
	 * regarding the postive result
	 * 
	 * @param thash - testhash of the positive result
	 */
	public void positiveTest(String thash) {
		if (thash != null || thash != "") {
			testhash.add(thash);
			positivetestflag = true;
		}
	}

	/**
	 * toHash hashes given string to hide the details
	 * 
	 * @param Individual - String to hash (device id)
	 * @return - resulted hash of given string Reference:
	 *         https://www.geeksforgeeks.org/sha-256-hash-in-java/
	 */
	private String toHash(String Individual) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			BigInteger number = new BigInteger(1, md.digest(Individual.getBytes(StandardCharsets.UTF_8)));
			StringBuilder hexString = new StringBuilder(number.toString(16));
			while (hexString.length() < 32) {
				hexString.insert(0, '0');
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception thrown for incorrect algorithm: " + e);
		}
		return null;
	}

	/**
	 * dateTodays method is used to convert date to number of days from 2021-01-01.
	 * 
	 * @param Date - date to convert
	 * @return - number of days from 2021-01-01 Reference:
	 *         https://beginnersbook.com/2017/10/java-8-calculate-days-between-two-dates/
	 */
	private int dateTodays(String Date) {
		String fromdate = "2021-01-01";
		long noOfDaysBetween;
		LocalDate dateBefore = LocalDate.parse(Date);
		LocalDate dateAfter = LocalDate.parse(fromdate);
		noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
		int i = (int) noOfDaysBetween;
		return Math.abs(i);
	}

	/**
	 * synchronizeData sync data to the government server
	 * 
	 * @return true - if contacted to any postive tested person else false
	 * 
	 */
	public boolean synchronizeData() {
		if (!(userhash == null || userhash == "")) {
			// System.out.println(toXml());
			Boolean r;
			r = gov.mobileContact(userhash, toXml());
			contacts.clear();
			positivetestflag = false;
			return r;
		}

		return false;
	}

	/**
	 * toXml method converts all the information that is contacts, deviceid and
	 * postive test if exists to string in XML format
	 * 
	 * @return XML String
	 */
	private String toXml() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		Element syncdata, hashofuser, contact_list, positivetesthash_list, contact, positivetesthash;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.newDocument();
			syncdata = dom.createElement("Synchronise_Data");
			hashofuser = dom.createElement("hashofuser");
			hashofuser.appendChild(dom.createTextNode(userhash));
			syncdata.appendChild(hashofuser);
			dom.appendChild(syncdata);
			contact_list = dom.createElement("contact_list");
			contact = dom.createElement("contact");
			if (contacts.size() >= 1) {
				for (int i = 0; i < contacts.size(); i++) {

					contact = dom.createElement("contact");
					Element contacted = dom.createElement("contacted");
					contacted.appendChild(dom.createTextNode(contacts.get(i).get(0)));
					Element date = dom.createElement("date");
					date.appendChild(dom.createTextNode(contacts.get(i).get(1)));
					Element duration = dom.createElement("duration");
					duration.appendChild(dom.createTextNode(contacts.get(i).get(2)));

					contact.appendChild(contacted);
					contact.appendChild(date);
					contact.appendChild(duration);
					contact_list.appendChild(contact);

				}
			}
			syncdata.appendChild(contact_list);
			if (positivetestflag == true) {
				positivetesthash_list = dom.createElement("positivetesthash_list");
				positivetesthash = dom.createElement("positivetesthash");
				for (int i = 0; i < testhash.size(); i++) {

					positivetesthash = dom.createElement("positivetesthash");
					positivetesthash.appendChild(dom.createTextNode(testhash.get(i)));

					positivetesthash_list.appendChild(positivetesthash);

				}
				syncdata.appendChild(positivetesthash_list);
			}

		} catch (Exception e) {
			System.out.println("Could not create XML" + e.getMessage());
			e.printStackTrace();
			return null;
		}
		try {
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = tf.newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(dom), new StreamResult(writer));
			xmldata = writer.getBuffer().toString();
			return xmldata;

		} catch (TransformerException te) {
			System.out.println(te.getMessage());
		}
		return null;
	}

}
