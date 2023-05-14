import requests

url = 'http://localhost:3000/Batch'

json_object = {
    'uid': '12432', 
    'batch': [
        {
            'non_translated': "Pw pw ante na doulepsei to spell checker asdfsadfasdkfk",
            'translated': "Πω πω άντε να δουλέψι το spell checker ασδφσαδφασδκφκ", 
        },
        {
            'non_translated': "O Xarhs einai kalo paidi;Etsi; Nomizw pws einai.",
            'translated': "Ο Χάρης είναι καλό παιδί;Έτσι; Νομίζω πως είναι."
        },
        {
            'non_translated': "O Kwstas einai kata tou vim. Giati re kWsta eisai kata tou vim?",
            'translated': "Ο Κώστας είναι κατά του vim. Γιατί ρε Κώστα είσαι κατά του Vim?"
        },
        {            
            'non_translated': "O Kwstas einai kata tou vim. Giati re kWsta eisai kata tou vim?Re kwsta se parakalw mhn eisai kata tou vim giati to vim einai polu kalo.",
            'translated': "Ο Κώστας είναι κατά του vim. Γιατί ρε Κώστα είσαι κατά του Vim?Ρε Κώστα σε παρακαλώ μην είσαι κατά του vim γιατί το vim είναι πολύ καλό."
        }
    ]
}


for i in range(200): 
    json_object["batch"].append(
        {
            'non_translated': "Mhn re file.",
            'translated': "Μην ρε φίλε."
        }
    )
# set the encoding of the request data to UTF-8
headers = {'Content-Type': 'application/json; charset=utf-8'}

# send the request with the UTF-8 encoding
r = requests.post(url, headers=headers, json=json_object)

print(r.status_code)


