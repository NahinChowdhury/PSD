const fs = require("fs");
const fetch = require("node-fetch");
const API_KEY = 'AIzaSyCKS84oxFhny5oeH6MUy6NI8GM4OYjvbCE';


async function init(){

    // we read all the poi locations and feed the info into poiInfo
    // we read all the SP between each poi and feed it into datasetInfo
    // then we send fetch request to distance matrix api which returns the SP between 2 locations
    // if there is no error, we update the value we had in datasetInfo
    // otherwise, we don't modify the value at all and keep the previous value we had for SP between 2 locs
    // in the end, after a 40sec wait, we write the new values into our file.

    const poiInfo = await readPOIInfo();
    const customerInfo = await readCustomerInfo();
    const datasetInfo = await readCurrentDataset();
    console.log("customer info here")
    console.log(customerInfo);

    let popCounter = 0;
    for(let line of datasetInfo){
        // console.log(line.length)
        if(line.length === 0){
            popCounter++;
            console.log("This line is blank")
        }
    }

    for(let i  = 0; i < popCounter; i++){
        // we pop the last element because we always have white spaces towards the end of file
        datasetInfo.pop();
    }
    console.log(datasetInfo)

    const len = poiInfo.length;
    // console.log(poiInfo[0]);
    // console.log(poiInfo[0][1]);


    // calling a fetch req for every single poi.
    // as we have 51 POIs, we need to make 51*51 = 2601 requests at once.

    // let count = 0;
    for(let i = 0; i < len; i++) {
            // count++;
            // await fetchDuration(customrInfo[0], customerInfo[1], poiInfo[i][1], poiInfo[i][2], datasetInfo, i);

            // console.log(`startLat: ${poiInfo[i][1]}, startLng: ${poiInfo[i][2]}, endLat: ${poiInfo[j][1]}, endLng: ${poiInfo[j][2]}`)
            // console.log(`arr[${i}][${j}]`);
    }
    // console.log(count);

    // setTimeout of sec because by then, we get all the
    setTimeout(() =>{
        // console.log(datasetInfo);

        const file = fs.createWriteStream('test.txt'); // algorithm reads from test.txt
        // const file = fs.createWriteStream('test2.txt'); // test2 is for debugging
        file.on('error', function(err) { /* error handling */ });
        datasetInfo.forEach(function(v) {
             file.write(v + '\n'); 
        });
        file.end();
    }, 05*1000)

    // console.log("after fetch")

    async function readPOIInfo(){
        const poiFile = fs.readFileSync("../datasets/poiLocations/poiLocations.txt").toString('utf-8');
        const poiLines = poiFile.split("\r\n");
    
        const poiInfo = [];
        for(let line of poiLines) {
            poiInfo.push(line.split(" "))
        }

        return poiInfo;
    }

    async function readCustomerInfo(){
        const customerFile = fs.readFileSync("../datasets/customerLocation/customerLocation.txt").toString('utf-8');
        const customerLines = customerFile.split(" ");
    
        // const customerInfo = [];
        // for(let line of customerLines) {
        //     customerInfo.push(line.split(" "))
        // }

        return customerLines;
    }

    async function readCurrentDataset(){
        // const datasetFile = fs.readFileSync("./test.txt").toString('utf-8');
        const datasetFile = fs.readFileSync("./test.txt").toString('utf-8');
        const datasetLines = datasetFile.split("\n"); // may need to remove \r as well
    
        // const datasetInfo = [];
        // for(let line of datasetLines) {
        //     datasetInfo.push(line.split(" "))
        // }

        // return datasetInfo;
        return datasetLines;
    }

    async function fetchDuration(startLat, startLng, endLat, endLng, arr, i){

            // console.log(`startLat: ${startLat}, startLng: ${startLng}, endLat: ${endLat}, endLng: ${endLng}`)
            
            // fetch(`https://maps.googleapis.com/maps/api/distancematrix/json?key=${API_KEY}&units=metric&departure_time=now&origins=${startLat},${startLng}&destinations=${endLat},${endLng}`) // correct url
            fetch(`https://maps.googleapis.com/maps/api/distancematrix/json?key=abc&units=metric&departure_time=now&origins=${startLat},${startLng}&destinations=${endLat},${endLng}`) // url with error
            .then(response =>{
                // console.log(response.status)
                if(response.status != 200){
                    throw new Error(`Error at arr[${i}]`);
                }else{
                    return response.json()
                }
            })
            .then(data => {
                // console.log(data);
                // console.log(data.rows[0].elements[0].duration_in_traffic.value)
                // console.log(`arr[${i}][${j}]`);
                // console.log(arr[i][j])
                // console.log("ok")
                
                arr[i] = data.rows[0].elements[0].duration_in_traffic.value;
            })
            // .then(resolve())
            .catch(error =>{
                console.log(`Error:arr[${i}]`)
                console.log(error);
            })
    }

}

init();