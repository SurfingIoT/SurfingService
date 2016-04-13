using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading;

namespace GlobalCode.Surfing.Device
{
    internal class SerialDeviceRxTx : IDevice
    {
        public int BaudRate { get; }
        Dictionary<string, Thing> things;
        List<Thing> thingsList;
        private SerialPort _serialPort;

        public SerialDeviceRxTx(string portName, int baudRate)
        {
            PortName = portName;
            BaudRate = baudRate;
            things = new Dictionary<string, Thing>();
            thingsList = new List<Thing>();
        }
        public string Name { get; set; }
        public string ResourceString { get; set; }
        public string Description { get; set; }
        public string Id { get; set; }
        public List<string> SendQueue { get; set; }
        public string PortName { get; set; }
        public Dictionary<string, Thing> Things { get; set; }
        public Collection<Thing> ThingsList { get; set; }
        public void Send(string s)
        {
            try
            {
                _serialPort?.Write(s);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        public string Receive()
        {
            try
            {
                if (_serialPort == null)
                {
                    var msg = $"This device ({Name}) is not working because IO objects are null. " +
                              "You should restart the device!";
                    throw new Exception(msg);
                }

                var available = _serialPort.BytesToRead;
                return available == 0 ? null : _serialPort.ReadExisting();
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        public void Close()
        {
            try
            {
                _serialPort?.Close();
                Connected = false;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        public void Open()
        {
            try
            {
                _serialPort = new SerialPort(PortName, BaudRate, Parity.None)
                {
                    DataBits = 8,
                    StopBits = StopBits.One
                };
                _serialPort.Open();
                Connected = true;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        public void Discovery()
        {
            throw new NotImplementedException();
        }

        public bool Connected { get; set; }

        public void AddEventListener()
        {
            throw new NotImplementedException();
        }

        public Timer TimerControl { get; set; }
    }
}
