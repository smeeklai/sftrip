<?php
class sms extends CI_Controller {

	public function __construct() {
		parent::__construct();
		/*
		 load you helper library
		 */
		/*
		 load you model
		 */
		$this -> load -> model('sms_model');
		$this -> load -> library('curl');
		$this->load->library('encrypt');
	}

	function index() {
		$email = $this -> input -> post("email");
		$tripID = $this -> input -> post("tripID");
		$sendingDataPeriod = $this->input->post("sendingDataPeriod");
		if ($email && $tripID != null) {
			$this -> sendSMS($email, $tripID, $sendingDataPeriod);
		}
	}

	/*function getTripLogInfo() {
		$key = $this->config->item('encryption_key');
		$encryptedTripID = $this -> uri -> segment(3, 0);
		$decyptedTripID = $this->encrypt->decode($encryptedTripID, $key);
		$result = $this -> sms_model -> getLogInfo($decyptedTripID);
		
		foreach ($result as $row) {
			echo $row -> latitude . "\n";
			echo $row -> longtitude . "\n";
		}
		//echo "$encryptedTripID \n";
		//echo "$shortenURL \n";
		echo "$decyptedTripID \n";
	}*/

	function shortenURL($url) {
		$apiKey = 'AIzaSyAgFdrAZ2AAULMOnDgpRZljeMTQ3Zskb9E';

		//Long to Short URL
		//$longUrl = 'http://hayageek.com/google-url-shortener-api/';
		$longUrl = $url;
		$postData = array('longUrl' => $longUrl, 'key' => $apiKey);
		$info = $this->httpsPost($postData);
		if ($info != null) {
			return $info -> id;
		} else {
			return "Can't contact to google server";
		}
	}

	function httpsPost($postData) {
		$curlObj = curl_init();

		$jsonData = json_encode($postData);

		curl_setopt($curlObj, CURLOPT_URL, 'https://www.googleapis.com/urlshortener/v1/url');
		curl_setopt($curlObj, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($curlObj, CURLOPT_SSL_VERIFYPEER, 0);
		curl_setopt($curlObj, CURLOPT_HEADER, 0);
		curl_setopt($curlObj, CURLOPT_HTTPHEADER, array('Content-type:application/json'));
		curl_setopt($curlObj, CURLOPT_POST, 1);
		curl_setopt($curlObj, CURLOPT_POSTFIELDS, $jsonData);

		$response = curl_exec($curlObj);

		//change the response json string to object
		$json = json_decode($response);
		curl_close($curlObj);

		return $json;
	}

	function sendSMS($email, $tripID, $sendingDataPeriod) {
		$key = 'OZNYZyAaEJGIVFvbdUWREAojNBWHn1CQ';
		$query = $this -> sms_model -> findRelativeNO($email);
		$a = $query[0] -> relativemobile1;
		$b = $query[0] -> relativemobile2;
		$c = $query[0] -> relativemobile3;
		$mobileNO = $a . "," . $b . "," . $c;
		
		$encryptedTripID = $this->encrypt->encode($tripID, $key, TRUE);
		$link = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/alert/show_info/" 
		. $encryptedTripID . "/" . $sendingDataPeriod;
		$shortenURL = $this->shortenURL($link);
		$SMSmsg = "SOS! message! Your relative is in emergency situation. Look their location at following: "
		. $shortenURL;

		$data['mobile'] = $mobileNO;
		$data['lan'] = "0";
		$data['message'] = $SMSmsg;
		$data['sendername'] = "SIT";
		$data['username'] = "53270327";
		$data['password'] = "pazuS5af";

		$result = $this -> curl -> simple_get('http://sms.qrtec.co.th/receive/qrbulksmsapi.asp', $data);
		if ($result == "0") {
			$arr['success'] = "1";
			$arr['error'] = "0";
			$arr['error_msg'] = "";
		} else {
			$arr['success'] = "0";
			$arr['error'] = "1";
			$arr['error_msg'] = "SMSs cannot be sent. Something is wrong on server side";
			$arr['result'] = $result;
		}
		echo json_encode($arr);
		//var_dump($result);
	}

	function UTF16Encode($msg) {
		$total = '';
		$msg = mb_convert_encoding($msg, 'UTF-16', 'UTF-8');
		For ($i = 0; $i < mb_strlen($msg, 'UTF-16'); $i++) {
			$total .= bin2hex(mb_substr($msg, $i, 1, 'UTF-16'));
		}
		Return strtoupper($total);
	}

}
?>