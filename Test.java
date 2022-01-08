import java.util.Scanner;

public class Test {

	private static final boolean False = false;
	private static final boolean True = true;
	
	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String configFile= "C:\\Users\\Geetanjali\\Documents\\SDC_Project\\Project\\src\\config";
		String configFile1= "C:\\Users\\Geetanjali\\Documents\\SDC_Project\\Project\\src\\config1";
		String configFileGovernment= "C:\\Users\\Geetanjali\\Documents\\SDC_Project\\Project\\src\\configg";
		Government government = new Government(configFileGovernment);
		MobileDevice Mobiledevice = new MobileDevice(configFile,government);
		MobileDevice Mobiledevice1 = new MobileDevice(configFile1,government);
		//YYYY-MM-DD
		Mobiledevice.recordContact("a", 10, 20);
		Mobiledevice.recordContact("b", 14, 10);
		Mobiledevice.recordContact("c", 20, 30);
		
		government.recordTestResult("HiTested",11,True);
		government.recordTestResult("Hi",13,True);
		government.recordTestResult("Hiiii",20,True);
		government.recordTestResult("Hiiieee",10,True);
		
		Mobiledevice.positiveTest("HiTested");
		Mobiledevice.positiveTest("Hi");
		
		System.out.println(Mobiledevice.synchronizeData());
		
		Mobiledevice.recordContact("110.0.245.98"+"iphone",5,30);
		
		Mobiledevice1.recordContact("x", 10, 10);
		Mobiledevice1.recordContact("y", 30, 20);
		Mobiledevice1.recordContact("z", 13, 40);
		Mobiledevice1.recordContact("135.0.245.98"+"random", 6, 20);
		Mobiledevice1.recordContact("b", 14, 50);
		Mobiledevice1.positiveTest("Hiiieee");
//		System.out.println(government.findGatherings(10,1,1,1));
		System.out.println(Mobiledevice1.synchronizeData());
		
		
		System.out.println(Mobiledevice.synchronizeData());
	}

}
