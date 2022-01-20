import SwiftUI


//Global
let PhoneNumber  = "+15555215554"

struct SMS: Codable
{
    let to: String
    let body: String
}

struct Transaction : Codable
{
    let TransactionID: Int
    let TransactionTimestamp: String
    let SourcePhone: String
    let DestinationPhone: String
    let TransactionAmount: Double

}

struct Group : Codable
{
    let GroupName: String
}

struct ContentView: View
{
    
    var body: some View
    {

        TabView
        {
            BalanceView().tabItem
            {
                Image(systemName: "house.fill")
                Text("Balance")
            }
            
            SendView().tabItem
            {
                Image(systemName: "paperplane.fill")
                Text("Send")
            }
            
            InvoiceView().tabItem
            {
                Image(systemName: "cart.fill")
                Text("Invoice")
            }
            
            GroupsView().tabItem
            {
                Image(systemName: "person.3.fill")
                Text("Groups")
            }
        }
        .accentColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
    }
}




struct BalanceView: View
{
    @State private var Balance: String = "0.00"
    @State private var Transactions : [Transaction] = Array()
    @State private var displayQR = false

    func fetchData()
    {
        Task
        {
                let (data, _) = try await URLSession.shared.data(from: URL(string:"http://localhost:3000/passSMS?phone=\(PhoneNumber)&message_body=2")!)
                let decodedResponse = try? JSONDecoder().decode( SMS.self, from: data)
                Balance = decodedResponse?.body.components(separatedBy: ",")[1] ?? ""
            
        }
        
        //Get Transactions
        Task
        {
                let (data, _) = try await URLSession.shared.data(from: URL(string:"http://localhost:3000/getTransactions?phone=\(PhoneNumber)")!)
                let decodedResponse = try? JSONDecoder().decode( [Transaction].self, from: data)
                Transactions = decodedResponse ?? Array()
            
        }
    }
    var body: some View
    {

    
    
    VStack
    {
        
        Color(red: 5/255, green: 96/255, blue: 235/255)
                .ignoresSafeArea() // Ignore just for the color
                .overlay(
                    VStack(alignment: .leading)
                    {
                        HStack
                        {
                            
                            Button
                            {
                                for family in UIFont.familyNames.sorted() {
                                    let names = UIFont.fontNames(forFamilyName: family)
                                    print("Family: \(family) Font names: \(names)")
                                }

                                
                                fetchData()

                            }
                            label:
                            {
                                Image(systemName: "arrow.clockwise")
                            }
                            .buttonStyle(.borderless)
                            .frame(alignment: .leading)
                            .foregroundColor(.white)
                            
                            
                            Text("Balance (LE)")
                                .font(Font.custom("kollektif-bold", size: 18))
                                .foregroundColor(.white)
                                .frame(height: 10, alignment: .leading)
                        }
                        .frame(width: .infinity, alignment: .leading)
                        .padding(20)
                        
                        Spacer()
                        
                        HStack(alignment: .center)
                        {
                            Spacer()
                            
                            Text(Balance)
                                .font(Font.custom("kollektif", size: 60))
                                .foregroundColor(.white)
                                .frame(width: .infinity, alignment: .center)
                                .padding(30)
                            
                            Spacer()
                        }
                        
                        
                        
                        Spacer()
                        
                        HStack
                        {
                            Button
                            {
                                print("Edit button was tapped")
                                displayQR = true
                            }
                            label:
                            {
                                Image(systemName: "qrcode")
                            }
                            .buttonStyle(.borderless)
                            .frame(width: 380, alignment: .trailing)
                            .foregroundColor(.white)
                            .padding(20)
                            .sheet(isPresented: $displayQR)
                            {
                                QRView(Data: String ("Phone,\(PhoneNumber)"), Title: "Phone")
                            }
                        }
                        
                        
                    })
                .frame(width: .infinity, height: 250, alignment: .leading)
        
        
        VStack(alignment: .leading)
        {
            Text("Transactions")
                .font((Font.custom("kollektif-bold", size: 20)))
                .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
                .multilineTextAlignment(.leading)
                .padding(.horizontal, 10.0)
                .frame(alignment: .leading)
                .padding (10)
            
            List
            {
                

                
                ForEach(Transactions, id: \.TransactionID)
                {
                    (transaction) in
                    
                    HStack
                    {
                        VStack(alignment: .leading)
                        {
                            Text(transaction.TransactionTimestamp)
                                    .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
                                    .font(Font.custom("kollektif-bold", size: 20))
                                 
                            Text((transaction.DestinationPhone != PhoneNumber) ? ("Send to: \(transaction.DestinationPhone)") : ("Receive from: \(transaction.SourcePhone)") )
                                    .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255))
                        }
                        Spacer()
                                 
                        Text(String(transaction.TransactionAmount))
                                    .font(Font.custom("kollektif-bold", size: 35))
                                    .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
                    }
                }
                
                
                
            }
            
            Spacer()
        }
       
            
    }.onAppear(perform: fetchData)

    }
}

struct QRView: View
{
    var Data: String
    var Title: String


    var body: some View
    {
        Text("\(Title) QR Code")
                .font(Font.custom("kollektif-bold", size: 30))
                .padding()
        
        AsyncImage(url: URL(string: "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=\(Data)"))
            .frame(width: 200, height: 200)
        

    }
}


struct SendView: View
{
    @State private var phone: String = ""
    @State private var amount: String = ""
    @State private var result: String = ""
    @State private var type: Int = 1
    @State private var displayAlert = false
    

    var body: some View
    {
        VStack
        {
            
            Color(red: 5/255, green: 96/255, blue: 235/255)
                .ignoresSafeArea() // Ignore just for the color
                .overlay(Text("SMSPay").font(Font.custom("kollektif", size: 35)).foregroundColor(.white))
                .frame(width: .infinity, height: 50, alignment: .topLeading)

            
            VStack (alignment:.leading)
            {
                

                
                HStack
                {
                    Text("Destination")
                        .font(Font.custom("kollektif-bold", size: 30))
                        .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255))
                        .multilineTextAlignment(.leading)
                        .padding(.top, 5.0)
                        
                    Picker(selection: $type, label: Text("Destination"))
                        {
                            Text("Phone").tag(1).foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
                            Text("Group").tag(3).foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )

                        }
                        .padding()
                        .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
                        .pickerStyle(SegmentedPickerStyle())

                }
            
                TextField("",text: $phone)
                
                Divider()
                    .padding(.horizontal, 10)
            
                Text("Amount")
                    .font(Font.custom("kollektif-bold", size: 30))
                    .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
                    .multilineTextAlignment(.leading)
            
                TextField("",text: $amount)
            
                Divider()
                    .padding(.horizontal, 10)
            
            
                Button("Send")
                {
                    Task
                    {
                            let (data, _) = try await URLSession.shared.data(from: URL(string:"http://localhost:3000/passSMS?phone=\(PhoneNumber)&message_body=\(type),\(amount),\(phone)")!)
                            let decodedResponse = try? JSONDecoder().decode( SMS.self, from: data)
                            result = decodedResponse?.body.components(separatedBy: ",")[0] ?? "No Response Receieved"
                    }
                    displayAlert = true;
                }
                .buttonStyle(.bordered)
                .frame(width: 380, alignment: .trailing)
                .alert (isPresented: $displayAlert)
                {
                    Alert (
                        title: Text("Transaction"),
                        message: Text (result),
                        dismissButton: .default(Text("Okay"), action: {})
                    )
                }
                
                Spacer()
            }
            .padding(.horizontal, 20.0)
        
        }
        .frame(alignment: .leading)

    }
}



struct InvoiceView: View {
    @State private var amount: String = ""
    @State private var displayQR: Bool = false

    var body: some View {
        
        
        VStack(alignment: .leading){
            
            Color(red: 5/255, green: 96/255, blue: 235/255)
                .ignoresSafeArea() // Ignore just for the color
                .overlay(

                    Text("SMSPay").font(Font.custom("kollektif", size: 35)).foregroundColor(.white)

                ).frame(width: .infinity, height: 50, alignment: .topLeading)
            
            VStack (alignment:.leading)
            {
            
            Text("Amount").font(Font.custom("kollektif-bold", size: 35)).foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) ).multilineTextAlignment(.leading).padding(.top, 5.0)
            
            TextField(
                    "",
                    text: $amount
                )
            Divider().padding(.horizontal, 10)
            
            Button("Generate QR") {
                displayQR = true
            }
            .buttonStyle(.bordered)
            .frame(width: 380, alignment: .trailing)
            .sheet(isPresented: $displayQR)
            {
                QRView(Data: String ("Invoice,\(PhoneNumber),\(amount)"), Title: "Invoice")
            }
            
            Spacer()
            }.padding(.horizontal, 15.0)
        
        }.frame(alignment: .leading)
    }
}

struct GroupsView: View
{
    @State private var amount: String = ""
    @State private var createGroup: Bool = false
    @State private var Groups : [Group] = Array()


    
    var body: some View
    {
        VStack(alignment: .leading)
        {
            
            Color(red: 5/255, green: 96/255, blue: 235/255)
                .ignoresSafeArea() // Ignore just for the color
                .overlay(
                    HStack
                    {
                        Text("Groups")
                            .font(Font.custom("kollektif-bold", size: 45))
                            .foregroundColor(.white)
                            .padding(.horizontal,15)
                        Spacer()
                        
                        Button()
                        {
                            createGroup = true;
                        }
                        label:
                        {
                            Image(systemName: "plus.circle")
                        }
                        .buttonStyle(.borderless)
                        .frame(alignment: .leading)
                        .foregroundColor(.white)
                        .sheet(isPresented: $createGroup)
                        {
                            CreateGroupView()
                        }
                        .padding(10)
                        
                        
                        
                        
                    }.padding(15))
                .frame(width: .infinity, height: 200, alignment: .topLeading)
            
            
            VStack(alignment: .leading)
            {
                List
                {
                    ForEach(Groups, id: \.GroupName)
                    {
                        (Group) in
                        
                        HStack
                        {
                            Text(String(Group.GroupName))
                                        .font(Font.custom("kollektif", size: 35))
                                        .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )

                        }
                    }
                    
                    
                }
                
                Spacer()
            }.onAppear(perform: fetchData)

        
        }.frame(alignment: .leading)
    }
    func fetchData()
    {
        Task
        {
                let (data, _) = try await URLSession.shared.data(from: URL(string:"http://localhost:3000/getGroups?phone=\(PhoneNumber)")!)
                let decodedResponse = try? JSONDecoder().decode( [Group].self, from: data)
                Groups = decodedResponse ?? Array()
            
        }
    }
}

    



struct CreateGroupView: View
{
    @State private var MembersSize: Int = 1
    @State private var GroupName: String = ""
    @State private var Members: [String] = [""]
    @State private var displayAlert: Bool = false
    @State private var result: String = ""


    var body: some View
    {
        
                VStack (alignment:.leading)
                {
                
                    Text("Group Name")
                        .font(Font.custom("kollektif-bold", size: 25))
                        .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
                        .multilineTextAlignment(.leading)
                        .padding(15)
                        .padding(.top, 25)

                    
                    TextField("",text: $GroupName)
                        .overlay(
                                RoundedRectangle(cornerRadius: 14).stroke(Color.gray, lineWidth: 1)
                                )
                        .padding(15)

                                
                    HStack
                    {

                        Text("Members")
                            .font(Font.custom("kollektif-bold", size: 25))
                            .foregroundColor(Color(red: 5/255, green: 96/255, blue: 235/255) )
                            .multilineTextAlignment(.leading)

                        Spacer()
                        
                        Button("+")
                        {
                            if (MembersSize < 10)
                            {
                                MembersSize += 1;
                                Members.append("")
                            }
                        }
                        

                        
                    }
                    .padding(15)
                
                    List
                    {
                    
                        ForEach((0...MembersSize-1), id: \.self)
                        {
                            TextField("",text: $Members[$0])
                                .overlay(
                                        Rectangle().stroke(Color.gray, lineWidth: 1)
                                )
                                .padding(15)
                        }
                    }

                    Button("Create")
                    {
                        Task
                        {
                            var Query : String = "http://localhost:3000/passSMS?phone=\(PhoneNumber)&message_body=4,\(GroupName),"
                            for i in 0...MembersSize-1
                            {
                                Query = Query + Members[i] + ","
                            }

                            let (data, _) = try await URLSession.shared.data(from: URL(string:Query)!)
                            let decodedResponse = try? JSONDecoder().decode( SMS.self, from: data)
                            result = decodedResponse?.body.components(separatedBy: ",")[0] ?? "No Response Receieved"
                        }
                        displayAlert = true;
                    }
                    .buttonStyle(.bordered)
                    .frame(width: 380, alignment: .trailing)
                    .alert (isPresented: $displayAlert)
                    {
                        Alert (
                            title: Text("Create Group"),
                            message: Text (result),
                            dismissButton: .default(Text("Okay"), action: {})
                        )
                    }
                    
                
                    Spacer()
                }
        }
    }



struct ContentView_Previews: PreviewProvider
{
    static var previews: some View
    {
        ContentView()
    }
}


