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
    const datasetInfo = await readCurrentDataset();

    let popCounter = 0;
    for(let line of datasetInfo){
        if(line.length === 1){
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
        for(let j = 0; j < len; j++){
            // count++;
            // await fetchDuration(poiInfo[i][1], poiInfo[i][2], poiInfo[j][1], poiInfo[j][2], datasetInfo, i, j);

            // console.log(`startLat: ${poiInfo[i][1]}, startLng: ${poiInfo[i][2]}, endLat: ${poiInfo[j][1]}, endLng: ${poiInfo[j][2]}`)
            // console.log(`arr[${i}][${j}]`);
        }
    }
    // console.log(count);

    // setTimeout of sec because by then, we get all the
    setTimeout(() =>{
        // console.log(datasetInfo);

        // const file = fs.createWriteStream('test.txt'); // algorithm reads from test.txt
        const file = fs.createWriteStream('test2.txt'); // test2 is for debugging
        file.on('error', function(err) { /* error handling */ });
        datasetInfo.forEach(function(v) {
             file.write(v.join(' ') + '\n'); 
        });
        file.end();
    }, 40*1000)

    // console.log("after fetch")

    async function readPOIInfo(){
        const poiFIle = fs.readFileSync("../datasets/poiLocations/poiLocations.txt").toString('utf-8');
        const poiLines = poiFIle.split("\r\n");
    
        const poiInfo = [];
        for(let line of poiLines) {
            poiInfo.push(line.split(" "))
        }

        return poiInfo;
    }

    async function readCurrentDataset(){
        // const datasetFile = fs.readFileSync("./test.txt").toString('utf-8');
        const datasetFile = fs.readFileSync("./test2.txt").toString('utf-8');
        const datasetLines = datasetFile.split("\n"); // may need to remove \r as well
    
        const datasetInfo = [];
        for(let line of datasetLines) {
            datasetInfo.push(line.split(" "))
        }

        return datasetInfo;
    }

    async function fetchDuration(startLat, startLng, endLat, endLng, arr, i, j){

            // console.log(`startLat: ${startLat}, startLng: ${startLng}, endLat: ${endLat}, endLng: ${endLng}`)
            
            // fetch(`https://maps.googleapis.com/maps/api/distancematrix/json?key=${API_KEY}&units=metric&departure_time=now&origins=${startLat},${startLng}&destinations=${endLat},${endLng}`) // correct url
            fetch(`https://maps.googleapis.com/maps/api/distancematrix/json?key=abc&units=metric&departure_time=now&origins=${startLat},${startLng}&destinations=${endLat},${endLng}`) // url with error
            .then(response =>{
                // console.log(response.status)
                if(response.status != 200){
                    throw new Error(`Error at arr[${i}][${j}]`);
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
                
                arr[i][j] = data.rows[0].elements[0].duration_in_traffic.value;
            })
            // .then(resolve())
            .catch(error =>{
                console.log(`Error:arr[${i}][${j}]`)
                console.log(error);
            })
    }

}

init();