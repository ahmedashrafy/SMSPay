// Libraries 
const express       = require("express");
const bodyParser    = require("body-parser");
const e = require("express");
const { response } = require("express");
const router        = express.Router();
const { generateKeyPair } = require('crypto');
var CryptoJS = require('crypto-js');


// Database Connection
const db = require("../controllers/db");
const { _ } = require("node-rsa/src/utils");

// Express parameters
router.use(bodyParser.json());                          // Automatically parse all POSTs as JSON.
router.use(bodyParser.urlencoded({ extended: true }));  // Automatically parse URL parameters




function encrypt(message = '', key = ''){
    return message; 
    return CryptoJS.AES.encrypt(message, key).toString();
}
function decrypt(message = '', key = ''){
    return message; 

    return CryptoJS.AES.decrypt(message, key).toString(CryptoJS.enc.Utf16);
}



function sleep(ms) 
{
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function sleep_test ()
{
    await sleep (5000);
}


  
//Gateway APIs
router.get("/passSMS", function(req, res) 
{
    let phone               = (req.query.phone[0] == '+')?(req.query.phone[0] = ' ') : req.query.phone;
    let message_body        = req.query.message_body;
    let Request             = parseMessage (phone, message_body); 
    let Response            = doOperation  (Request, res); 

    /*let query = "SELECT * FROM Accounts WHERE Phone ='"+phone+"';"
    db.query(query, function (err, result)
    {
        if (err  || result.length == 0)
            decrypted_message_body = message_body;
        else
            decrypted_message_body = decrypt (message_body, result[0].EncryptionKey);

        console.log ("here")
        console.log (decrypted_message_body)
        console.log ("here")

       
    }); */


});



//Gateway API helpers
function decryptMessage (phone, message_body)
{
    return message_body; 
    let query = "SELECT * FROM Accounts WHERE Phone ='"+phone+"';"
    db.query(query, function (err, result)
    {
        if (err  || result.length == 0)
            return message_body; 
        else
            return decrypt (message_body, result[0].EncryptionKey);
    }); 
}


//Gateway API helpers
function encryptMessage (phone, message_body)
{
    let query = "SELECT * FROM Accounts WHERE Phone ='"+phone+"';"
    db.query(query, function (err, result)
    {
        if (err  || result.length == 0)
            return message_body; 
        else
            return encrypt (message_body, result[0].EncryptionKey);
    }); 
}


function parseMessage (phone, message_body)
{

    switch (message_body.split(",") [0])
    {
        case '0': 
        {
            return ({
                "op"            :"0",
                "phone"         :phone, 
                "pub_key"       :message_body.split(",")[1], 
            })
        }

        case '1': 
        { 
            return ({
                "op"        : "1",
                "from"      :phone, 
                "to"        :message_body.split(",")[2], 
                "amount"    :message_body.split(",")[1],
            })
        }

        case '2': 
        { 
            return ({
                "op"        : "2",
                "from"      :phone, 
            })
        }

        case '3': 
        { 
            return ({
                "op"        : "3",
                "from"      :phone,
                "amount"     :message_body.split(",")[1],
                "group"     :message_body.split(",")[2],
            })
        }

        case '4': 
        { 
            return ({
                "op"        : "4",
                "from"      :phone, 
                "name"      :message_body.split(",")[1],
                "to"        :message_body.split(",").slice (2)
            })
        }
    }

}

function doOperation (Request, res)
{

    switch (Request.op)
    {
        case '0': 
        {
            opRegistration (Request, res); 
            break; 
        }  
        case '1': 
        {
            opTransfer (Request, res); 
            break; 
        }
        case '2': 
        {
            opBalance (Request, res); 
            break; 
        
        }
        case '3': 
        {
            opGroupTransfer (Request, res); 
            break; 
        }

        case '4': 
        {
            opCreateGroup (Request, res); 
            break; 
        }
    }
}

function opBalance (Request, res)
{
    let balance_validation_query  =  "SELECT Balance FROM Accounts WHERE Phone = '"+Request.from+"'";

    db.query(balance_validation_query, function (err, result) 
    {
        if (err) 
        {
            res.json ( {
                        'to':Request.from, 
                        'body':decryptMessage(Request.from, "ERR_USER_NOT_REGISTERED"),
                        })
         
        }
        else
        {
            res.json ({
                    'to':Request.from, 
                    'body':decryptMessage(Request.from, "CONF_TX_SUCCESS,"+result[0].Balance),
                });
        }

    });   
}

function opCreateGroup (Request, res)
{
    var resolved = false;

    for (let i = 0; i<Request.to.length; i++)
    {
        let query = "Insert into PaymentGroups(GroupName, CreatorPhone, DestinationPhone) values ('" +Request.name +"', '"+Request.from+"', '"+Request.to[i]+"'); ";
        db.query(query, function (err, result) 
        {
            if (err) 
            {
                console.log (err)
             
            }
            else
            {
                if (!resolved)
                {

                        res.json ({
                            'to':Request.from, 
                            'body': "CONF_REQUEST_SENT"
                        });
                    resolved  = true; 
                }
            }
    
        });   

    }
   
}

function opRegistration (Request, res)
{
    let user_query  = "Insert into Accounts(Phone, EncryptionKey, Balance) values ('" +Request.phone +"', '"+Request.pub_key+"', 0); ";

    db.query(user_query, function (user_err, user_result) 
    {
        if (user_err) 
        {
            console.log (user_err)
            res.json ( 
                {
                    'to':Request.phone, 
                    'body':"ERR_USER_ALREADY_EXISTS,0"
                })
        }

        else
        {
            res.json ( 
                {
                    'to':Request.phone, 
                    'body':"CONF_USER_CREATED,",
                })
        }
            


    });   
}

function opTransfer (Request, res)
{
    let balance_validation_query  =  "SELECT Balance FROM Accounts WHERE Phone = '"+Request.from+"'";

    db.query(balance_validation_query, function (src_error, src_balance) 
    {
        if (src_error) 
        {   
            console.log (src_error)
            res.json (
                {
                    'to':Request.from, 
                    'body':decryptMessage(Request.from,"ERR_SRC_NOT_REGISTERED"),
                }    
            )
        }
        else
        {
            if (src_balance[0].Balance < Request.amount)  
            {
                res.json (
                    {
                        'to':Request.from, 
                        'body':decryptMessage(Request.from,"ERR_INSUF_BALANCE,"+src_balance[0].Balance)
                    }    
                )
            }

            else
            {
                let dest_validation_query  =  "SELECT * FROM Accounts WHERE Phone = '"+Request.to+"'";

                db.query(dest_validation_query, function (dest_error, dest_balance) 
                {
                    if (dest_error)
                    {

                        res.json (
                            {
                                'to':Request.from, 
                                'body':decryptMessage(Request.from,"ERR_UNKNOWN")
                            }    
                        )
                    }
                    else
                    {
                        if (dest_balance.length === 0)
                        {
                            res.json (
                                {
                                    'to':Request.from, 
                                    'body':decryptMessage(Request.from,"ERR_DEST_NOT_REGISTERED")
                                }    
                            )
                        }

                        else
                        {
                            let src_update_query    = "UPDATE Accounts SET Balance =  "+ String(Number(src_balance[0].Balance) - Number(Request.amount)) +  " WHERE  Phone ='"+Request.from+"';";
                            let dest_update_query   = "UPDATE Accounts SET Balance = " + String(Number(dest_balance[0].Balance) + Number(Request.amount)) +  " WHERE  Phone ='"+Request.to+"';";
                            let tx_update_query   = "Insert into Transactions(SourcePhone, DestinationPhone, TransactionAmount) values ('"+Request.from+"', '"+Request.to+"',"+Request.amount+");"; 

                            let update_query        = src_update_query + dest_update_query + tx_update_query; 
                            db.query(update_query, function (tx_error, tx_success) 
                            {
                                if (tx_error)
                                {
                                    console.log (tx_error);
                                    res.json (
                                        {
                                            'to':Request.from, 
                                            'body':decryptMessage(Request.from,"ERROR_TX_FAILED")
                                        }    
                                    )
                                }
                                else
                                {
                                    console.log (src_balance);
                                    res.json (
                                        {
                                            'to':Request.from, 
                                            'body':decryptMessage(Request.from, "CONF_TX_SUCCESS,"+(src_balance[0].Balance - Request.amount)),
                                        }    
                                    )
                                }
                            });


                        }


                    }
                });
            }
        }
    });   
}

function opGroupTransfer (Request, res)
{

    let balance_validation_query  =  "SELECT Balance FROM Accounts WHERE Phone = '"+Request.from+"'";

    db.query(balance_validation_query, function (balance_error, balance_result) 
    {
        if (balance_error)
        {
            console.log (balance_error)

            res.json (
                {
                    'to':Request.from, 
                    'body':"ERR_UNKNOWN",
                }    
            )

        }
        else
        {
            let group_members_query = "SELECT DestinationPhone FROM PaymentGroups WHERE CreatorPhone = '"+Request.from+"' AND GroupName = '"+ Request.group + "';" ;

            db.query(group_members_query, function (group_error, group_result) 
            {

                console.log (group_result)

                if (group_error)
                {
                    res.json (
                        {
                            'to':Request.from, 
                            'body':"ERR_DEST_NOT_REGISTERED"
                        }    
                    )
                }

                
                else if (group_result.length * Request.amount > balance_result[0].Balance)
                {
                    res.json (
                        {
                            'to':Request.from, 
                            'body':"ERR_INSUF_BALANCE"
                        }    
                    )
                }

                else
                {

                    let src_update_query    = "UPDATE Accounts SET Balance =  "+ String(Number(balance_result[0].Balance) - (Number(group_result.length) * Number(Request.amount))) +  " WHERE  Phone ='"+Request.from+"';"
                    db.query(src_update_query, function (error, result) {} );



                    for (let i = 0; i<group_result.length; i++)
                    {


                        let group_balance_query  =  "SELECT Balance FROM Accounts WHERE Phone = '"+group_result[i].DestinationPhone+"'";


                        db.query(group_balance_query, function (group_balance_error, group_balance_result) 
                        {
                            if (group_balance_error)
                            {
                                res.json (
                                    {
                                        'to':Request.from, 
                                        'body':"ERR_UNKNOWN",
                                    }    
                                )
                    
                            }
                            else
                            {
                                let transaction_update = "UPDATE Accounts SET Balance = " + String(Number(group_balance_result[0].Balance) + Number(Request.amount)) +  " WHERE  Phone ='"+group_result[i].DestinationPhone+"';";
                                transaction_update += "Insert into Transactions(SourcePhone, DestinationPhone, TransactionAmount) values ('"+Request.from+"', '"+group_result[i].DestinationPhone+"',"+Request.amount+");"; 
                                db.query(transaction_update, function (error, result)  {if (error) console.log (error)});
                            }

                        });
                    }

                    res.json (
                        {
                            'to':Request.from, 
                            'body':"CONF_TX_SUCCESS"
                        }    
                    ) 
                    
                }
            });
        }
    });
}


//Web APIs
router.get("/getTransactions", function(req, res) 
{
    let phone               = req.query.phone;

    let send_query  = "SELECT * FROM Transactions WHERE  SourcePhone ="+req.query.phone;
    let recv_query  = "SELECT * FROM Transactions WHERE  DestinationPhone ="+req.query.phone;
    let query  = send_query + " UNION ALL " + recv_query + " ORDER BY TransactionTimestamp DESC LIMIT 5";


    db.query(query, function (err, result) 
    {
        if (err) 
        {
            res.json (err);
         
        }
        else
        {
            for (let i = 0; i<result.length; i++)
                result[i].TransactionTimestamp = result[i].TransactionTimestamp.toDateString();
                
            res.json (result);

        }

    });   

});

router.get("/getGroups", function(req, res) 
{
    let phone               = req.query.phone;

    let group_query  = "SELECT DISTINCT GroupName FROM PaymentGroups WHERE CreatorPhone = '"+req.query.phone+"';";


    db.query(group_query, function (err, result) 
    {
        if (err) 
        {
            res.json (err);
         
        }
        else
        {
            res.json (result);

        }

    });   

});

router.get ("/grantMoney", function (req, res)
{
    let query    = "UPDATE Accounts SET Balance =  15000"; 
    db.query(query, function (err, result)
    {
        if (err)
            res.json({"success":0});
        else
            res.json({"success":1});    

    });
});

router.get ("/createAccount", function (req, res)
{
    let phone               = req.query.phone;

    let user_query  = "Insert into Accounts(Phone, EncryptionKey, Balance) values ('" + phone +"','"+"N/A"+"', 0); ";

    db.query(user_query, function (user_err, user_result) 
    {
        if (user_err) 
        {
            res.json ( 
                {
                    'to':phone, 
                    'body':user_err
                })
        }

        else
        {
            res.json ( 
                {
                    'to':phone, 
                    'body':"CONF_USER_CREATED,",
                })
        }
            


    });   
});

router.get ("/getGroupInfo", function (req, res)
{
    let phone               = req.query.phone;
    let group               = req.query.group;

    let query = "SELECT DestinationPhone FROM PaymentGroups WHERE CreatorPhone = '"+phone+"' AND GroupName = '"+ group + "';" ;

    db.query(query, function (user_err, user_result) 
    {
        if (user_err) 
        {
            res.json ( 
                {
                    'to':phone, 
                    'body':user_err
                })
        }

        else
        {
            res.json ( 
                {
                    'to':phone, 
                    'body':user_result,
                })
        }
            


    });   
});

// Export the created router
module.exports = router;

