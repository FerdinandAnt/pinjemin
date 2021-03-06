<?php
	require_once("functions/globals.php");

	$var_PID = isset($_POST['PID'])? $_POST["PID"] : -1;
	$var_ownUID = isset($_POST['ownUID'])? $_POST["ownUID"] : -1;

	// escape characters
	$var_PID = addslashes($var_PID);
	$var_ownUID = addslashes($var_ownUID);
	
	$conn = openConnection();
	$sql = "call getThreads($var_PID, $var_ownUID)";
	$result = $conn->query($sql);

	$response = array();
	
	// write file
	$file = fopen("log.txt", "a");
	fwrite($file, $sql . "\n");
	fclose($file);
   
	// if there is no error
	if ($result != false) {
		while ($row = $result->fetch_assoc()) {
			array_push($response, $row); 
		}
		
   	echo json_encode(array('server_response'=>$response));
	}
	
?>