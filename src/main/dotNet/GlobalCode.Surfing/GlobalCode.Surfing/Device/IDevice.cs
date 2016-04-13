using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Threading;

namespace GlobalCode.Surfing.Device
{
    /// <summary>
    /// Interface de um Device
    /// </summary>
    public interface IDevice
    {
        string Name { get; set; }
        string ResourceString { get; set; }
        string Description { get; set; }
        string Id { get; set; }
        List<string> SendQueue { get; set; }
        string PortName { get; set; }
        Dictionary<string, Thing> Things { get; set; }
        Collection<Thing> ThingsList { get; set; }
        void Send(string s);
        string Receive();
        void Close();
        void Open();
        void Discovery();
        bool Connected { get; set; }
        void AddEventListener();
        Timer TimerControl { get; set; }
    }
}
