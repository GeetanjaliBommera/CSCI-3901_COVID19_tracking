import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Java code for Government
 * 
 * @author Geetanjali Bommera B00881511 Government class takes all data from
 *         different mobile devices and stores in centralized database. It also
 *         informs mobile device user if any of his contacts tested positive.
 */

public class Government {
	/**
	 * Global variables to share between methods
	 */
	private String domain, userid, password;
	Connection connection = null;
	int maxdate;
	/**
	 * Government method is the constructor. It takes database, user, password for
	 * the centralized database from a file to connect.
	 * 
	 * @param configFile
	 */
	public Government(String configFile) {
		// TODO Auto-generated constructor stub
		if (configFile == null || configFile == "") {
			System.exit(0);
		}
		try {
			File myObj = new File(configFile);
			Scanner myReader = new Scanner(myObj);
			int flag = 0;
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				if (flag == 0) {
					String dname[] = data.split("=");
					domain = dname[1];
					flag = 1;

				} else if (flag == 1) {
					String dname[] = data.split("=");
					userid = dname[1];
					flag = 2;
				} else if (flag == 2) {
					String dname[] = data.split("=");
					password = dname[1];
					flag = 0;
				}

			}
			myReader.close();
			
			
		} catch (FileNotFoundException e) {
			System.out.println("Error in config file - Mobile Device");
			e.printStackTrace();
		} 
		LocalDate localDate = LocalDate.now();//For reference
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
		String formattedString = localDate.format(formatter);
		maxdate = dateTodays(formattedString);

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
	 * recordTestResult method stores testhash for the individuals once tested if
	 * postive
	 * 
	 * @param testHash - testhash of individual
	 * @param date     - date of test
	 * @param result   - result of the test
	 */
	public void recordTestResult(String testHash, int date, boolean result) {
		PreparedStatement preStatement1 = null;
		String temp = String.valueOf(result);
		try {
			if (result && connecttoDB()) {
				if(0<=date && date<=maxdate) {
				preStatement1 = this.connection.prepareStatement(
						"insert ignore into testresults(testhash,date,result,mhash) values(?, ?, ?, Null);");
				preStatement1.setString(1, testHash);
				preStatement1.setInt(2, date);
				preStatement1.setString(3, temp);
				preStatement1.execute();
				closeconnectDB();
				}
			}
		} catch (SQLException y) {
			// handling errors
			System.out.println("SQLException: " + y.getMessage());
			System.out.println("SQLState: " + y.getSQLState());
			System.out.println("VendorError: " + y.getErrorCode());

		}
	}
	private boolean connecttoDB() {
		
		try {
			this.connection = DriverManager.getConnection(domain, userid, password);
			if(connection != null) {
				return true;
			}
			 return false;
		}catch (SQLException y) {
			// handling errors
			System.out.println("SQLException: " + y.getMessage());
			System.out.println("SQLState: " + y.getSQLState());
			System.out.println("VendorError: " + y.getErrorCode());

		}
		return false;
		
	}
	private boolean closeconnectDB() {
		
		try {
			connection.close();
			return true;
		}catch (SQLException y) {
			// handling errors
			System.out.println("SQLException: " + y.getMessage());
			System.out.println("SQLState: " + y.getSQLState());
			System.out.println("VendorError: " + y.getErrorCode());

		}
		return false;
	}
	/**
	 * mobileContact is called by mobiledevice class that syncs mobile device data
	 * in XML to centralized database
	 * 
	 * @param initiator   - Deviceid of the mobile device
	 * @param contactInfo - Information in XML.
	 * @return true if any contact is tested positive else false
	 */
	public boolean mobileContact(String initiator, String contactInfo) {
		if (initiator == null || initiator == "") {
			return false;
		}
		if(!connecttoDB()) {
			return false;
		}
		PreparedStatement preStatement = null, preStatement1 = null;
		ResultSet resultset1, resultset2 = null;
		String userhash = null;
		ArrayList<String> positivehash = new ArrayList<String>();
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<ArrayList<String>> contacts = new ArrayList<ArrayList<String>>();
		ArrayList<String> positives = new ArrayList<String>();
		try {
			InputStream stream = new ByteArrayInputStream(contactInfo.getBytes(StandardCharsets.UTF_8));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(stream);
			dom.getDocumentElement().normalize();
			// mobilehash
			Node hashofuser = dom.getElementsByTagName("hashofuser").item(0);
			if (hashofuser != null) {
				userhash = hashofuser.getTextContent();
			}
			// contacts
			NodeList contact_list = dom.getElementsByTagName("contact");
			for (int i = 0; i < contact_list.getLength(); i++) {
				Node contact = contact_list.item(i);

				if (contact.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) contact;
					contacts.add(new ArrayList<String>());
					contacts.get(i).add(0, e.getElementsByTagName("contacted").item(0).getTextContent());
					contacts.get(i).add(1, e.getElementsByTagName("date").item(0).getTextContent());
					;
					contacts.get(i).add(2, e.getElementsByTagName("duration").item(0).getTextContent());
					;

				}
			}
			// positivehash
			NodeList positivetesthash_list = dom.getElementsByTagName("positivetesthash");
			for (int i = 0; i < positivetesthash_list.getLength(); i++) {
				Node positivetesthash = positivetesthash_list.item(i);
				positivehash.add(positivetesthash.getTextContent());

			}

			if (connection != null) {
				if (userhash != null) {
					preStatement = connection.prepareStatement("INSERT ignore INTO mobilehash VALUES(?)");
					preStatement.setString(1, initiator);
					preStatement.execute();
					preStatement = null;

					if (contacts.size() > 0) {
						for (int i = 0; i < contacts.size(); i++) {
							preStatement = connection.prepareStatement("INSERT IGNORE INTO mobilehash VALUES(?)");
							preStatement.setString(1, contacts.get(i).get(0));
							preStatement.execute();
							preStatement = null;
						}

						for (int i = 0; i < contacts.size(); i++) {

							preStatement = connection.prepareStatement("INSERT ignore INTO contacts VALUES(?,?,?,?)");
							preStatement.setString(1, initiator);
							for (int j = 0; j < contacts.get(i).size(); j++) {
								int k = 2;
								preStatement.setString(k + j, contacts.get(i).get(j));

							}
							preStatement.execute();
							preStatement = null;
						}
					}

					if (positivehash.size() > 0) {
						for (int i = 0; i < positivehash.size(); i++) {
							preStatement = connection
									.prepareStatement("UPDATE ignore testresults SET mhash = ? WHERE testhash = ?");
							preStatement.setString(1, initiator);
							preStatement.setString(2, positivehash.get(i));

							preStatement.executeUpdate();
							preStatement = null;
						}
					}
					// returns only when new positive contact is detected
					preStatement1 = connection.prepareStatement(
							"select t.testhash,t.date as testeddate,c.neighbourid,c.date as contactedate from testresults as t inner join contacts as c on t.mhash = c.neighbourid where c.userid =?");
					preStatement1.setString(1, initiator);
					resultset1 = preStatement1.executeQuery();
					ArrayList<String> temptesthash = new ArrayList<String>();
					while (resultset1.next())
						if (Math.abs(resultset1.getInt(2) - resultset1.getInt(4)) <= 14) {
							temptesthash.add(resultset1.getString(1));

						}
					preStatement1 = connection.prepareStatement("Select count(testhash) from shown where mhash = ?");
					preStatement1.setString(1, initiator);
					resultset2 = preStatement1.executeQuery();
					while (resultset2.next()) {
						if (resultset2.getInt(1) < temptesthash.size()) {
							for (int i = 0; i < temptesthash.size(); i++) {
								preStatement = connection.prepareStatement("insert ignore into shown values(?,?)");
								preStatement.setString(1, initiator);
								preStatement.setString(2, temptesthash.get(i));
							}
							return true;
						}
					}

				}
			}

		} catch (Exception e) {
			System.out.println("Error in XML");
			e.printStackTrace();
		}finally {
			closeconnectDB();
		}
		return false;
	}

	public int findGatherings(int date, int minSize, int minTime, float density) {
		if(!connecttoDB()) {
			return 0;
		}
		int gatherings=0;
		PreparedStatement preStatement1 = null;
		ResultSet resultset1 = null;
		Set<String> uniqueids = new HashSet<String>();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		HashMap<String, HashSet<String>> neighbours = new HashMap<String, HashSet<String>>();
		HashMap<Set<String>, HashSet<String>> intersect = new HashMap<Set<String>, HashSet<String>>();
		try {

			preStatement1 = this.connection
					.prepareStatement("select userid,neighbourid from contacts where date = ? and duration>=?");
			preStatement1.setInt(1, date);
			preStatement1.setInt(2, minTime);
			resultset1 = preStatement1.executeQuery();
			while (resultset1.next()) {
				ArrayList<String> temp = new ArrayList<String>();
				temp.add(resultset1.getString(1));
				temp.add(resultset1.getString(2));
				result.add(temp);
			}

			for (int i = 0; i < result.size(); i++) {
				for (int j = 0; j < result.get(i).size(); j++) {
					uniqueids.add(result.get(i).get(j));
				}
			}
			Iterator value = uniqueids.iterator();
			while (value.hasNext()) {
				String s = String.valueOf(value.next());
				neighbours.put(s, null);
			}

			Set<Entry<String, HashSet<String>>> entrySet = neighbours.entrySet();
			Iterator<Entry<String, HashSet<String>>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				String s = iterator.next().getKey();
				HashSet<String> t = new HashSet<String>();
				if(iterator.next().getValue()!=null) {
				t.addAll(iterator.next().getValue());
				for (int i = 0; i < result.size(); i++) {
					if (s.equals(result.get(i).get(0))) {
						t.add(result.get(i).get(1));
						t.add(s);
						neighbours.replace(s, t);
					}
				}
				}
			}
			while (resultset1.next()) {
				String a = resultset1.getString(1);
				String b = resultset1.getString(2);
				HashSet<String> each = new HashSet<String>();
				each.add(a);
				each.add(b);
				HashSet<String> temp = new HashSet<String>();
				temp.addAll(neighbours.get(a));
				temp.addAll(neighbours.get(b));
				intersect.put(each, temp);
			}
			Set<Entry<Set<String>, HashSet<String>>> entrySet1 = intersect.entrySet();
			Iterator<Entry<Set<String>, HashSet<String>>> iterator1 = entrySet1.iterator();
			while(iterator1.hasNext()) {
				int count =0;
			ArrayList<String> each = new ArrayList<String>();
				each.addAll(iterator.next().getValue());
				if(each.size()>=minSize) {
				for(int i=0;i<each.size();i++) {
					for(int j=0;j<each.size();j++) {
						if(!each.get(i).equals(each.get(j))) {
							String a = each.get(i);
							String b = each.get(j);
							for(int k=0;j<result.size();j++) {
								if(result.get(0).get(k).equals(a)) {
									
									count ++;
								}
							}
								
							
						}
					}
				}
				
				int m = (count*(count-1))/2;
				if(m>density) {
					gatherings++;
				}
				}
				
				}
		} catch (SQLException y) {
			// handling errors
			System.out.println("SQLException: " + y.getMessage());
			System.out.println("SQLState: " + y.getSQLState());
			System.out.println("VendorError: " + y.getErrorCode());

		}
		closeconnectDB();
		return gatherings;
	}

}
