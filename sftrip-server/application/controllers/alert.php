<?php
class Alert extends CI_Controller {
	public function __construct() {
		parent::__construct();
		/*
		 load you helper library
		 */
		/*
		 load you model
		 */
		$this->load->helper('url');
		$this -> load -> model('alert_model');
		$this -> load -> library('googlemaps');
		$this->load->library('encrypt');
	}
	
	function show_info() {
		$url = $this -> uri -> segment(3, 0);
		$refreshPeriod = (int)$this->uri->segment(4, 0);
		$refreshPeriod *= 60;
		$this->output->set_header("refresh:$refreshPeriod;url=");
		$key = $this->config->item('encryption_key');
		$encryptedTripID = $this -> uri -> segment(3, 0);
		$decyptedTripID = $this->encrypt->decode($encryptedTripID, $key);
		//$decyptedTripID = 359;
		
		$query = $this -> alert_model -> get_location($decyptedTripID);
		$query2 = $this -> alert_model -> get_taxi_info($decyptedTripID);
		
		//add gray markers to show all path that already pass
		foreach ($query as $row) {
			$lat = $row -> latitude;
			$lng = $row -> longtitude;
			$marker = array();
			$marker['position'] = "$lat,$lng";
			$marker['icon'] = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/img/marker_gray2.png";
			$this -> googlemaps -> add_marker($marker);
		}
		
		//add blue marker to show start location
		foreach ($query2 as $row) {
			$startLat = $row -> startLat;
			$startLng = $row -> startLng;
			$marker = array();
			$marker['position'] = "$startLat,$startLng";
			$marker['infowindow_content'] = "$startLat, $startLng";
			$marker['icon'] = 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=S|9999FF|000000';
			$this -> googlemaps -> add_marker($marker);
		}

		//add green marker to show destination
		foreach ($query2 as $row) {
			$desLat = $row -> desLat;
			$desLng = $row -> desLng;
			$marker = array();
			$marker['position'] = "$desLat,$desLng";
			$marker['infowindow_content'] = "$desLat, $desLng";
			$marker['icon'] = 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=D|00ff00|000000';
			$this -> googlemaps -> add_marker($marker);
		}
		
		//add red marker to show current location
		$lat_current = $query[sizeof($query) - 1] -> latitude;
		$lng_current = $query[sizeof($query) - 1] -> longtitude;
		$marker = array();
		$marker['position'] = "$lat_current,$lng_current";
		$marker['infowindow_content'] = "$lat_current, $lng_current";
		$this -> googlemaps -> add_marker($marker);
		
		$config['center'] = "$lat_current, $lng_current";
		$config['zoom'] = 'auto';
		$this -> googlemaps -> initialize($config);

		$data['result'] = $this -> alert_model -> get_taxi_info($decyptedTripID);

		$data['map'] = $this -> googlemaps -> create_map();
		$this -> load -> view('alert_view', $data);
	}
}
?>