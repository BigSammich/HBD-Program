/**
This is a program to determine, for each web page in a collection, the number
of other web pages that link to it.
This program will first connect to a web page via the command line or by d
default if no URL is provided via command line. The program will then parse
web page and store any links that need to be visited. The program will then
visit those links, but only connect if it hasn't visited it already. While
visiting these links, the program will track how many links each web page has
pointing to it, and will also keep track of how many links were visited. The
web pages and the number of links they have will then be printed at the end.
 */

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Queue;
//import java.util.Vector;
import java.io.*;
import java.net.*;
import java.util.*;

/**
This class handles the visiting of links and keeping track of the data.
This class has variables that will handle the storage of the links and also
the tracking of the data which will be outputted at the end. This class has
methods to parse the host, connect to a web page, parse a web page and store
links, visit those links, and then print those web pages along with the
number of links they each have.
@author  Kyle Zimmermann
 */
public class HBD
{
   private static final String DEFAULT_START_URL =
         "http://www.whatifsports.com/HBD/Pages/Manager/EditLineups.aspx";
   private static final String DATA_PREFIX = "<td class=\"raw\"";
   private static final int HTTP_PORT = 80;
   private static Map<String, Integer> urlMap =
         new HashMap<String, Integer>();
   private static Queue<String> linksQueue = new LinkedList();
   private static Queue<String> pageLinksQueue = new LinkedList();
   private static Socket socket = null;
   private static PrintWriter out = null;
   private static BufferedReader in = null;
   private static String request = "";
   private static Integer linksVisited = 0;
   private static boolean foundLink = false;

   /**
   This is a method to get the host from a URL.
   The method will parse the URL and return a string containing a host,
   which will be connected to later.
   @param   A string variable, inUrl, which contains a URL .
   @return  A string containing the host.
    */
   private static String parseHost(String inUrl)
   {
      return inUrl.substring(DATA_PREFIX.length(),
            inUrl.indexOf('/', DATA_PREFIX.length()));
   }

   /**
   This is a method to get the path from a URL.
   The method will parse the URL and return a string containing a path.
   @param   A string variable, inUrl, which contains a URL.
   @return  A string containing the path.
    */
   private static String parseFile(String inUrl)
   {
      return inUrl.substring(inUrl.indexOf('/',
            DATA_PREFIX.length()), inUrl.length());
   }

   /**
   This is a method to get a URL from a line of text.
   This method will parse a line of text and return a URL, which will be
   visited later.
   @param   A string variable, line, which is the line to process and get
            the URL.
   @return  A string containing a URL.
    */
   private static String URLParser(String line)
   {
      String tempString = "";
      tempString = line.substring(line.indexOf(DATA_PREFIX),
            line.length());
      tempString = tempString.substring(tempString.indexOf(">"),
            tempString.indexOf('<'));
      return tempString;
   }

   /**
   This is a method to initially setup the connection to a URL.
   The method will create a new socket and attempt to connect to the
   host. It will put the starting URL in the urlMap and increment the
   linksVisited variable. The method will also catch an IOException and
   print out the specific error.
   @param   A string variable, startUrl, which is the URL that the
            program initially connects to.
   @return  Nothing, void method.
    */
   private static void Setup(String startUrl)
   {
      try
      {
         System.out.println("Attempting to connect to " + startUrl);
         socket = new Socket(parseHost(startUrl), HTTP_PORT);
         System.out.println("Connection made!");
         out = new PrintWriter(socket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(
               socket.getInputStream()));
         out.write(request);
         out.flush();

         urlMap.put(startUrl, 0);
         linksVisited++;
         System.out.println("Connection Closed!");
      }
      catch (IOException ex)
      {
         System.out.println("No connection made!");
         System.out.println("An IO Exception was caught: " + ex.toString());
      }
   }

   /**
   This is a method to connect to a URL.
   The method gets passed a URL and then creates a request message by
   calling the parseFile and paresHost methods to get that specific data.
   The method will then create a new socket and attempt to connect to the
   host. The method will also catch an IOException and print out the
   specific error.
   Does nothing if name doesn't appear in the list.
   @param   A string variable URL, which is the URL that the program will
            connect to.
   @return  Nothing, void method.
    */
   private static void Connect(String url)
   {
      try
      {
         request = "GET " + parseFile(url) + " HTTP/1.1\nHost: "
               + parseHost(url) + "\nConnection: close\n\n";
         System.out.println("Attempting to connect to " + url);
         socket = new Socket(parseHost(url), HTTP_PORT);
         System.out.println("Connection made!");
         out = new PrintWriter(socket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         out.write(request);
         out.flush();
         System.out.println("Connection Closed!");
      }
      catch (IOException ex)
      {
         System.out.println("No connection made!");
         System.out.println("An IO Exception was caught: " + ex.toString());
      }
   }

   /**
   This is a method to store all the links of a web page.
   The method first reads in a line of text and checks to see if it contains
   http:, which signifies a URL. While that line contains http:, the program
   will call URLParser to get the URL from that line of text. The URL, and
   all URL's on the line and web page, are stored in the pageLinksQueue.
   Those URLs are then stored in the pageLinksQueue linksQueue, which holds
   all the links from all the web pages. This method will also catch an
   IOException if it occurs and print out the corresponding message.
   @param   None
   @return  Nothing, void method.
    */
   private static void StoreLinks()
   {
      String theUrl = "";
      String theLine = "";
      try
      {
         String line = in.readLine();
         while (line != null)
         {
            theLine += " " + line;
            if(theLine.contains("<td class=\"r"))
               foundLink = true;
//            while (line.contains("<td class=\"raw\""))
//            {
//               theUrl = URLParser(line);
//               if (!pageLinksQueue.contains(theUrl))
//               {
//                  pageLinksQueue.add(theUrl);
//               }
//               line = line.substring(theUrl.length());
//            }
            line = in.readLine();
         }
         linksQueue.addAll(pageLinksQueue);
         pageLinksQueue.clear();
      }
      catch (IOException ex)
      {
         System.out.println("An IO Exception was caught: " + ex.toString());
      }
   }

   /**
   This is a method to visit a link in the linksQueue.
   The method will remove a link from the linksQueue and then check to see
   if it is in the urlMap. If it isn't in the map, then the program will put
   it in with a value of 1, meaning it has 1 link to it. The method will then
   call the Connect method, passing it the URL, and connect to the link. The
   StoreLinks method is then called and all links in that URL will be stored
   appropriately. The linksVisited variable is then incremented. However, if
   the link was in the map, then the method will just increment that links 
   value by 1.
   @param   None
   @return  Nothing, void method.
    */
   private static void VisitLink()
   {
      String currentUrl = "";
      currentUrl = linksQueue.remove();
      if (!urlMap.containsKey(currentUrl))
      {
         urlMap.put(currentUrl, 1);
         Connect(currentUrl);
         StoreLinks();
         linksVisited++;
      }
      else
      {
         urlMap.put(currentUrl, urlMap.get(currentUrl) + 1);
      }
   }

   /**
   This is a method to sort the URLs by value and then print them.
   This method first copies the urlMap to a temporary map. It then stores all
   the keys in a vector (keys) and stores all the values in a vector (values).
   The method then goes through and finds the max value and prints that and
   the corresponding key. This process is repeated based on the number of
   URLs in the urlMap.
   @param   None
   @return  Nothing, void method.
    */
   private static void SortAndPrint()
   {
      int maxValue = -1, maxIndex = 0;
      String tempUrl = "";
      Map<String, Integer> tempMap = new HashMap<String, Integer>();
      tempMap.putAll(urlMap);
      Vector<String> keys = new Vector<String>(tempMap.keySet());
      Vector<Integer> values = new Vector<Integer>(tempMap.values());
      for (int i = 0; i < urlMap.size(); i++)
      {
         maxValue = -1;
         for (int j = 0; j < values.size(); j++)
         {
            if (values.get(j) > maxValue)
            {
               maxValue = values.get(j);
               tempUrl = keys.get(j);
               maxIndex = j;
            }
         }
         values.remove(maxIndex);
         keys.remove(maxIndex);
         System.out.println(tempUrl + " " + maxValue);
         System.out.println(foundLink);
      }
   }

   /**
   This is a method to print the results of each web page.
   The method first checks to see how many web pages were stored, so that it
   can print out the appropriate message. The method then calls the 
   SortAndPrint method to sort out the urlMap by value and then print out all
   the URLs and their corresponding value. The method then prints a message
   indicating that the program is complete.
   @param   None
   @return  Nothing, void method.
    */
   private static void PrintResults()
   {
      if (urlMap.size() > 1)
      {
         System.out.println(linksVisited + " links visited.");
      }
      else
      {
         System.out.println(linksVisited + " link visited.");
      }

      SortAndPrint();
      System.out.println();
      System.out.println("Done. Normal termination.");
   }

   private static void fetchURL (String urlString,String user,String pass) {

try {
URL url;
URLConnection urlConn;
DataOutputStream printout;
DataInputStream input;

url = new URL (urlString);
urlConn = url.openConnection();
urlConn.setDoInput (true);

urlConn.setDoOutput (true);

urlConn.setUseCaches (false);

urlConn.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");

printout = new DataOutputStream (urlConn.getOutputStream ());

String content = "USERNAME=" + URLEncoder.encode (user) + "&PASSWORD=" + URLEncoder.encode (pass);

printout.writeBytes (content);
printout.flush ();
printout.close ();

input = new DataInputStream (urlConn.getInputStream ());
FileOutputStream fos=new FileOutputStream("postto.txt");
String str;
while (null != ((str = input.readLine())))
{

if (str.length() >0)
{
fos.write(str.getBytes());
fos.write(new String("\n").getBytes());
}
}
input.close ();
}
catch(MalformedURLException mue){ System.out.println (mue);}
catch(IOException ioe){ System.out.println (ioe);}
}
   /**
   This is the main method of the program which runs it.
   This method first checks to see if a URL was given via command line. If
   none was provided then the method uses the default URL. The method then
   runs the Setup method passing it the starting URL which sets up the
   initial connection. Then the method calls the StoreLinks method to store
   all the links of that starting URL. The method then keeps calling the
   VisitLink method until there are no more links to be visited. The
   PrintResults method is then called, which prints out all the URLs and
   their values.
   @param   An array of strings, args.
   @return  Nothing, void method.
    */
   public static void main(String args[])
   {
      fetchURL("http://www.whatifsports.com/HBD/Pages/Manager/EditLineups.aspx", "bigsammich","kmart6");
      String startUrl = "";
      if (args.length == 0)
      {
         startUrl = DEFAULT_START_URL;
         request = "GET " + parseFile(DEFAULT_START_URL)
               + " HTTP/1.1\nHost: " + parseHost(DEFAULT_START_URL)
               + "\nConnection: close\n\n";
      }
      else
      {
         startUrl = args[0];
         request = "GET " + parseFile(startUrl)
               + " HTTP/1.1\nHost: " + parseHost(startUrl)
               + "\nConnection: close\n\n";
      }
      Setup(startUrl);
      StoreLinks();
      while (!linksQueue.isEmpty())
      {
         VisitLink();
      }
      PrintResults();
   }
}
