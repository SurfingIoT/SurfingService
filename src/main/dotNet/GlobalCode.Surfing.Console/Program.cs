using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using GlobalCode.Surfing.Device;

namespace GlobalCode.Surfing.Console
{
    class Program
    {
        static void Main(string[] args)
        {
            IoTSurfboard board = null;
            try
            {
                board = new IoTSurfboard("COM5", 9600);
                while (true)
                {
                    //System.Console.WriteLine("Alcohol      :" + board.Alcohol());
                    System.Console.WriteLine("Temperature  :" + board.Temperature());
                    System.Console.WriteLine("Humidity     :" + board.Humidity());
                    System.Console.WriteLine("Light        :" + board.Light());
                    System.Console.WriteLine("Potentiometer:" + board.Potentiometer());
                    System.Console.WriteLine("Clock        :" + board.Clock());
                    System.Console.WriteLine("Red Light");
                    board.Red(255);
                    Thread.Sleep(500);
                    board.Red(0);
                    Thread.Sleep(500);
                    System.Console.WriteLine("Green Light");
                    board.Green(255);
                    Thread.Sleep(500);
                    board.Green(0);
                    Thread.Sleep(500);
                    System.Console.WriteLine("Blue Light");
                    board.Blue(255);
                    Thread.Sleep(500);
                    board.Blue(0);
                    Thread.Sleep(500);
                    System.Console.WriteLine("Relay");
                    board.Relay(true);
                    Thread.Sleep(500);
                    board.Relay(false);
                    Thread.Sleep(500);
                    System.Console.ReadKey();
                }
            }
            catch (Exception ex)
            {
                System.Console.WriteLine(ex.Message);
            }
            finally
            {
                board?.Close();
            }
        }
    }
}
