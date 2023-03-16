import requests

url = 'http://localhost:3000/Batch'

json_object = {
    'uid': '12432', 
    'batch': [
        {
            'non_translated': "O Xarhs einai kalo paidi.",
            'translated': "Ο Χάρης είναι καλό παιδί."
        },
        {
            'non_translated': "O Kwstas einai kata tou vim. Giati re kWsta eisai kata tou vim?",
            'translated': "Ο Κώστας είναι κατά του vim. Γιατί ρε Κώστα είσαι κατά του Vim;"
        }   
    ]
}
# set the encoding of the request data to UTF-8
headers = {'Content-Type': 'application/json; charset=utf-8'}

# send the request with the UTF-8 encoding
r = requests.post(url, headers=headers, json=json_object)

print(r.status_code)


