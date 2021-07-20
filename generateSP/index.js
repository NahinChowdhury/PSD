const fs = require("fs");
const fetch = require("node-fetch");
const API_KEY = 'AIzaSyCKS84oxFhny5oeH6MUy6NI8GM4OYjvbCE';


async function init(){
    const poiFIle = fs.readFileSync("../datasets/poiLocations/poiLocations.txt").toString('utf-8');
    const poiLines = poiFIle.split("\r\n");

    const poiInfo = [];
    for(let line of poiLines) {
        poiInfo.push(line.split(" "))
    }

    const len = poiInfo.length;
    // console.log(poiInfo[0]);
    // console.log(poiInfo[0][1]);

    const finalOutput = [];
    for(let i = 0; i < len; i++){
        const  finalRow = [];
        for(let j = 0; j < len; j++){
            finalRow.push([]);
        }
        finalOutput.push(finalRow);
    }
    // console.log(finalOutput)

    for(let i = 0; i < len; i++) {
        // const finalRow = [];
        for(let j = 0; j < len; j++){
            // console.log(`arr[${i}][${j}]`);
            // make the api req here
            // try to await it before making next request;

            await fetchDuration(poiInfo[i][1], poiInfo[i][2], poiInfo[j][1], poiInfo[j][2], finalOutput, i, j);
            // console.log(`startLat: ${poiInfo[i][1]}, startLng: ${poiInfo[i][2]}, endLat: ${poiInfo[j][1]}, endLng: ${poiInfo[j][2]}`)
        }
        // finalOutput.push(finalRow);
        // console.log("\n\n")
    }

    // setTimeout of 5sec because by then, we get all the
    setTimeout(() =>{
        console.log(finalOutput);
        // for(let row of finalOutput){
        //     console.log(row.length);
        // }
        // console.log("\nfinalOutput length:" + finalOutput.length)

        // const file = fs.createWriteStream('test.txt');
        // file.on('error', function(err) { /* error handling */ });
        // finalOutput.forEach(function(v) {
        //      file.write(v.join(' ') + '\n'); 
        // });
        // file.end();
    }, 40000)

    // console.log("after fetch")



    async function fetchDuration(startLat, startLng, endLat, endLng, arr, i, j){
            // console.log(`startLat: ${startLat}, startLng: ${startLng}, endLat: ${endLat}, endLng: ${endLng}`)
            // const resultPromise = new Promise(function(resolve, reject) {
            fetch(`https://maps.googleapis.com/maps/api/distancematrix/json?key=${API_KEY}&units=metric&departure_time=now&origins=${startLat},${startLng}&destinations=${endLat},${endLng}`)
            .then(response =>{
                // console.log(response.status)
                if(response.status != 200){
                    throw new Error(`Error at arr[${i}][${j}]`);
                }else{
                    return response.json()
                }
            })
            .then(data => {
                // if(data.rows[0].elements[0].duration_in_traffic.value == 0){
                // console.log(data);
                // console.log(data.rows[0].elements[0].duration_in_traffic.value)
                // console.log(`arr[${i}][${j}]`);
                // console.log(arr[i][j])
                arr[i][j] = data.rows[0].elements[0].duration_in_traffic.value;
                // console.log("ok")
            })
            // .then(resolve())
            .catch(error =>{
                console.log(error);
                // reject();
            })

        // });
        
        // await resultPromise;
    }

}

init();